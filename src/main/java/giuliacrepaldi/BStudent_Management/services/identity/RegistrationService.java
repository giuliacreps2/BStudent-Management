package giuliacrepaldi.BStudent_Management.services.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.*;
import giuliacrepaldi.BStudent_Management.enums.identity.RegistrationRequestStatus;
import giuliacrepaldi.BStudent_Management.enums.identity.Status;
import giuliacrepaldi.BStudent_Management.enums.identity.VerificationStatus;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.exceptions.UnauthorizedException;
import giuliacrepaldi.BStudent_Management.payloads.identity.GuardianConsentDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.RegisterAccountUserDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.RegistrationPreviewDTO;
import giuliacrepaldi.BStudent_Management.repositories.identity.*;
import giuliacrepaldi.BStudent_Management.security.OpaqueTokens;
import giuliacrepaldi.BStudent_Management.services.identity.events.RegistrationCompleted;
import giuliacrepaldi.BStudent_Management.services.identity.events.RegistrationConfirmationRequested;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IRegistrationService;
import giuliacrepaldi.BStudent_Management.utilities.Emails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;

@Slf4j
@Service
public class RegistrationService implements IRegistrationService {

    static final String PRIVACY_DOCUMENT_CODE = "privacy_policy";
    static final String STUDENT_ROLE = "STUDENT";
    static final String GUARDIAN_ROLE = "GUARDIAN";

    private static final String INVALID_LINK = "Link non valido o scaduto";
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final RegistrationRequestsRepository registrationRequestsRepository;
    private final UsersRepository usersRepository;
    private final StudentsRepository studentsRepository;
    private final RolesRepository rolesRepository;
    private final SchoolsRepository schoolsRepository;
    private final GuardianLinksRepository guardianLinksRepository;
    private final PolicyAcceptancesRepository policyAcceptancesRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    private final int digitalConsentAge;
    private final Duration requestTtl;
    private final int maxSends;
    private final Duration resendCooldown;
    private final String currentPolicyVersion;

    public RegistrationService(RegistrationRequestsRepository registrationRequestsRepository,
                               UsersRepository usersRepository,
                               StudentsRepository studentsRepository,
                               RolesRepository rolesRepository,
                               SchoolsRepository schoolsRepository,
                               GuardianLinksRepository guardianLinksRepository,
                               PolicyAcceptancesRepository policyAcceptancesRepository,
                               PasswordEncoder passwordEncoder,
                               ApplicationEventPublisher eventPublisher,
                               @Value("${registration.digital-consent-age}") int digitalConsentAge,
                               @Value("${registration.request-ttl}") Duration requestTtl,
                               @Value("${registration.max-sends}") int maxSends,
                               @Value("${registration.resend-cooldown}") Duration resendCooldown,
                               @Value("${registration.policy-version}") String currentPolicyVersion) {
        this.registrationRequestsRepository = registrationRequestsRepository;
        this.usersRepository = usersRepository;
        this.studentsRepository = studentsRepository;
        this.rolesRepository = rolesRepository;
        this.schoolsRepository = schoolsRepository;
        this.guardianLinksRepository = guardianLinksRepository;
        this.policyAcceptancesRepository = policyAcceptancesRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.digitalConsentAge = digitalConsentAge;
        this.requestTtl = requestTtl;
        this.maxSends = maxSends;
        this.resendCooldown = resendCooldown;
        this.currentPolicyVersion = currentPolicyVersion;
    }

    private static String hashOrInvalid(String token) {
        if (token == null || token.isBlank()) {
            throw new NotFoundException(INVALID_LINK);
        }
        return OpaqueTokens.hash(token);
    }

    private static int ageOf(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private static String studentNameOf(RegistrationRequest request) {
        return request.getFirstName() != null ? request.getFirstName() : request.getUsername();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    //REGISTRAZIONE
    @Override
    @Transactional
    public void register(RegisterAccountUserDTO body) {
        requireCurrentPolicy(body.policyVersion());

        String email = Emails.normalize(body.email());
        String username = body.username().trim();
        // L'età la calcola il server: il client non decide se serve il genitore
        boolean requiresGuardianConsent = ageOf(body.birthDate()) < digitalConsentAge;
        // Dai 14 anni il consenso è autonomo: l'email del genitore non viene conservata
        String guardianEmail = requiresGuardianConsent ? Emails.normalize(body.guardianEmail()) : null;

        if (requiresGuardianConsent && (guardianEmail == null || guardianEmail.isEmpty())) {
            throw new BadRequestException("Per gli studenti sotto i " + digitalConsentAge
                    + " anni è obbligatoria l'email del genitore o tutore");
        }
        if (email.equals(guardianEmail)) {
            throw new BadRequestException("L'email del genitore o tutore deve essere diversa da quella dello studente");
        }
        if (isUsernameTaken(username, email)) {
            throw new BadRequestException("Username già in uso");
        }
        if (usersRepository.existsByEmailIncludingDeleted(email)) {
            log.info("Registrazione ignorata: email già associata a un account");
            return;
        }

        //TODO VERIFICARE CHE LA SCUOLA NON SIA BLOCCANTE NELLA REGISTRAZIONE
        // Una nuova registrazione con la stessa email sostituisce quella ancora aperta
        registrationRequestsRepository.findByEmailNormalizedAndStatus(email, RegistrationRequestStatus.pending)
                .ifPresent(previous -> {
                    registrationRequestsRepository.delete(previous);
                    registrationRequestsRepository.flush();
                });

        School school = body.schoolId() == null ? null
                : schoolsRepository.findById(body.schoolId())
                .orElseThrow(() -> new NotFoundException(body.schoolId()));

        String rawToken = OpaqueTokens.generate();
        OffsetDateTime now = OffsetDateTime.now();
        RegistrationRequest request = RegistrationRequest.builder()
                .emailNormalized(email)
                .passwordHash(passwordEncoder.encode(body.password()))
                .firstName(trimToNull(body.firstName()))
                .lastName(trimToNull(body.lastName()))
                .birthDate(body.birthDate())
                .username(username)
                .school(school)
                .requiresGuardianConsent(requiresGuardianConsent)
                .guardianEmailNormalized(guardianEmail)
                .policyVersion(body.policyVersion())
                .tokenHash(OpaqueTokens.hash(rawToken))
                .expiresAt(now.plus(requestTtl))
                .lastSentAt(now)
                .build();
        registrationRequestsRepository.save(request);

        publishConfirmation(request, rawToken);
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationPreviewDTO preview(String token) {
        RegistrationRequest request = registrationRequestsRepository.findByTokenHash(hashOrInvalid(token))
                .filter(this::isOpen)
                .orElseThrow(() -> new NotFoundException(INVALID_LINK));

        boolean guardianHasAccount = request.isRequiresGuardianConsent()
                && usersRepository.findByEmailNormalized(request.getGuardianEmailNormalized()).isPresent();

        return new RegistrationPreviewDTO(studentNameOf(request), request.getUsername(),
                request.isRequiresGuardianConsent(), guardianHasAccount,
                currentPolicyVersion, request.getExpiresAt());
    }

    //CONFERMA: studente dai 14 anni
    @Override
    @Transactional
    public User confirmEmail(String token) {
        RegistrationRequest request = lockOpenRequest(token);
        if (request.isRequiresGuardianConsent()) {
            throw new BadRequestException("Questa registrazione richiede il consenso del genitore o tutore");
        }

        OffsetDateTime now = OffsetDateTime.now();
        // Ha cliccato il link ricevuto sulla propria casella: email verificata
        Student student = createStudentAccount(request, now);
        User studentUser = student.getUser();
        studentUser.setEmailVerifiedAt(now);
        recordPolicyAcceptance(studentUser, request.getPolicyVersion());

        registrationRequestsRepository.delete(request);
        eventPublisher.publishEvent(new RegistrationCompleted(studentUser.getEmailNormalized(),
                studentNameOf(request), null));
        log.info("Registrazione confermata: utente {}", studentUser.getUserId());
        return studentUser;
    }

    //CONSENSO: studente sotto i 14 anni
    @Override
    @Transactional
    public User confirmGuardianConsent(String token, GuardianConsentDTO body, User currentUser) {
        RegistrationRequest request = lockOpenRequest(token);
        if (!request.isRequiresGuardianConsent()) {
            throw new BadRequestException("Questa registrazione non richiede il consenso del genitore o tutore");
        }
        if (!body.consentGiven()) {
            throw new BadRequestException("Per completare la registrazione serve il consenso del genitore o tutore");
        }
        requireCurrentPolicy(body.policyVersion());

        OffsetDateTime now = OffsetDateTime.now();
        User guardian = resolveGuardian(request, body, currentUser, now);
        recordPolicyAcceptance(guardian, body.policyVersion());

        // L'email dello studente non è ancora verificata: il link è arrivato al genitore
        Student student = createStudentAccount(request, now);

        GuardianLink link = GuardianLink.builder()
                .guardian(guardian)
                .student(student)
                .relationship(body.relationship())
                .verificationStatus(VerificationStatus.verified)
                .verifiedAt(now)
                .consentDocumentVersion(body.policyVersion())
                .build();
        guardianLinksRepository.save(link);

        registrationRequestsRepository.delete(request);
        eventPublisher.publishEvent(new RegistrationCompleted(student.getUser().getEmailNormalized(),
                studentNameOf(request), guardian.getEmailNormalized()));
        log.info("Consenso registrato: genitore {} per studente {}", guardian.getUserId(), student.getStudentId());
        return student.getUser();
    }

    @Override
    @Transactional
    public void rejectGuardianConsent(String token) {
        RegistrationRequest request = lockOpenRequest(token);
        if (!request.isRequiresGuardianConsent()) {
            throw new BadRequestException("Questa registrazione non richiede il consenso del genitore o tutore");
        }
        registrationRequestsRepository.delete(request);
        log.info("Consenso rifiutato: richiesta {} cancellata", request.getRegistrationRequestId());
    }

    //REINVIO LINK
    @Override
    @Transactional
    public void resendConfirmation(String email) {
        OffsetDateTime now = OffsetDateTime.now();
        // Nessun errore se la richiesta non esiste o i limiti sono superati: non si rivela nulla
        registrationRequestsRepository
                .findByEmailNormalizedAndStatus(Emails.normalize(email), RegistrationRequestStatus.pending)
                .filter(this::isOpen)
                .ifPresent(request -> {
                    boolean tooSoon = request.getLastSentAt() != null
                            && request.getLastSentAt().plus(resendCooldown).isAfter(now);
                    if (request.getSendCount() >= maxSends || tooSoon) {
                        log.info("Reinvio ignorato per la richiesta {}: limite raggiunto",
                                request.getRegistrationRequestId());
                        return;
                    }
                    // Il token precedente smette di funzionare
                    String rawToken = OpaqueTokens.generate();
                    request.setTokenHash(OpaqueTokens.hash(rawToken));
                    request.setSendCount((short) (request.getSendCount() + 1));
                    request.setLastSentAt(now);
                    publishConfirmation(request, rawToken);
                });
    }

    //PULIZIA
    @Override
    @Transactional
    @Scheduled(cron = "${registration.purge-cron:0 0 3 * * *}")
    public void purgeExpired() {
        int deleted = registrationRequestsRepository.deleteExpired(OffsetDateTime.now());
        if (deleted > 0) {
            log.info("Cancellate {} richieste di registrazione scadute", deleted);
        }
    }

    // Il genitore già registrato deve essere autenticato; altrimenti crea qui il proprio account
    private User resolveGuardian(RegistrationRequest request, GuardianConsentDTO body, User currentUser,
                                 OffsetDateTime now) {
        String guardianEmail = request.getGuardianEmailNormalized();

        User existing = usersRepository.findByEmailNormalized(guardianEmail).orElse(null);
        if (existing != null) {
            if (currentUser == null || !existing.getUserId().equals(currentUser.getUserId())) {
                throw new UnauthorizedException("Accedi con l'account del genitore o tutore per confermare il consenso");
            }
            existing.getRoles().add(role(GUARDIAN_ROLE));
            return existing;
        }

        if (usersRepository.existsByEmailIncludingDeleted(guardianEmail)) {
            throw new BadRequestException("L'account del genitore o tutore non è attivo: contatta l'assistenza");
        }
        if (currentUser != null) {
            throw new BadRequestException("Sei autenticato con un account diverso da quello del genitore o tutore indicato: esci e riapri il link");
        }
        if (body.password() == null || body.password().length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("Imposta una password di almeno " + MIN_PASSWORD_LENGTH
                    + " caratteri per il tuo account");
        }

        // Ha aperto il link ricevuto sulla propria casella: email verificata
        User guardian = User.builder()
                .emailNormalized(guardianEmail)
                .passwordHash(passwordEncoder.encode(body.password()))
                .firstName(trimToNull(body.firstName()))
                .lastName(trimToNull(body.lastName()))
                .status(Status.active)
                .emailVerifiedAt(now)
                .build();
        guardian.getRoles().add(role(GUARDIAN_ROLE));
        return usersRepository.save(guardian);
    }

    // Ricontrolla l'unicità: tra la richiesta e la conferma qualcuno può aver preso email o username
    private Student createStudentAccount(RegistrationRequest request, OffsetDateTime now) {
        if (usersRepository.existsByEmailIncludingDeleted(request.getEmailNormalized())) {
            throw new BadRequestException("Esiste già un account con questa email");
        }
        if (studentsRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username già in uso: ripeti la registrazione con un altro username");
        }

        User user = User.builder()
                .emailNormalized(request.getEmailNormalized())
                .passwordHash(request.getPasswordHash())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .birthDate(request.getBirthDate())
                .status(Status.active)
                .build();
        user.getRoles().add(role(STUDENT_ROLE));
        usersRepository.save(user);

        Student student = Student.builder()
                .username(request.getUsername())
                .school(request.getSchool())
                .user(user)
                .build();
        return studentsRepository.save(student);
    }

    private void recordPolicyAcceptance(User user, String policyVersion) {
        if (!policyAcceptancesRepository.existsByUserUserIdAndDocumentCodeAndDocumentVersion(
                user.getUserId(), PRIVACY_DOCUMENT_CODE, policyVersion)) {
            policyAcceptancesRepository.save(PolicyAcceptance.builder()
                    .user(user)
                    .documentCode(PRIVACY_DOCUMENT_CODE)
                    .documentVersion(policyVersion)
                    .build());
        }
    }

    private RegistrationRequest lockOpenRequest(String token) {
        return registrationRequestsRepository.findForUpdateByTokenHash(hashOrInvalid(token))
                .filter(this::isOpen)
                .orElseThrow(() -> new NotFoundException(INVALID_LINK));
    }

    private boolean isOpen(RegistrationRequest request) {
        return request.getStatus() == RegistrationRequestStatus.pending
                && request.getExpiresAt().isAfter(OffsetDateTime.now());
    }

    private void publishConfirmation(RegistrationRequest request, String rawToken) {
        String recipient = request.isRequiresGuardianConsent()
                ? request.getGuardianEmailNormalized()
                : request.getEmailNormalized();
        eventPublisher.publishEvent(new RegistrationConfirmationRequested(recipient, studentNameOf(request),
                rawToken, request.isRequiresGuardianConsent(), request.getExpiresAt()));
    }

    private void requireCurrentPolicy(String policyVersion) {
        if (!currentPolicyVersion.equals(policyVersion)) {
            throw new BadRequestException("L'informativa privacy è stata aggiornata: ricarica la pagina e rileggila");
        }
    }

    private boolean isUsernameTaken(String username, String email) {
        return studentsRepository.existsByUsername(username)
                || registrationRequestsRepository.existsByUsernameAndStatusAndEmailNormalizedNot(
                username, RegistrationRequestStatus.pending, email);
    }

    private Role role(String code) {
        return rolesRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Ruolo " + code + " mancante: applicare la migrazione v0.3"));
    }
}
