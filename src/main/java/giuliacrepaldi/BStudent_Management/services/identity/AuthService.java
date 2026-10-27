package giuliacrepaldi.BStudent_Management.services.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.AuthToken;
import giuliacrepaldi.BStudent_Management.entities.identity.Role;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.identity.Status;
import giuliacrepaldi.BStudent_Management.enums.identity.TokenPurpose;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.exceptions.UnauthorizedException;
import giuliacrepaldi.BStudent_Management.payloads.identity.AuthResult;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoggedUserDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoginDTO;
import giuliacrepaldi.BStudent_Management.repositories.identity.AuthTokensRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.UsersRepository;
import giuliacrepaldi.BStudent_Management.security.OpaqueTokens;
import giuliacrepaldi.BStudent_Management.security.TokenTools;
import giuliacrepaldi.BStudent_Management.services.identity.events.EmailVerificationRequested;
import giuliacrepaldi.BStudent_Management.services.identity.events.PasswordChanged;
import giuliacrepaldi.BStudent_Management.services.identity.events.PasswordResetRequested;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IAuthService;
import giuliacrepaldi.BStudent_Management.utilities.Emails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthService implements IAuthService {

    private static final String INVALID_CREDENTIALS = "Credenziali non valide";
    private static final String INVALID_LINK = "Link non valido o scaduto";

    private final UsersRepository usersRepository;
    private final StudentsRepository studentsRepository;
    private final AuthTokensRepository authTokensRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenTools tokenTools;
    private final ApplicationEventPublisher eventPublisher;

    private final Duration passwordResetTtl;
    private final Duration emailVerificationTtl;
    private final Duration tokenRequestCooldown;

    // Hash fittizio: anche senza utente il login esegue un confronto BCrypt,
    // così i tempi di risposta non rivelano quali email sono registrate
    private final String dummyPasswordHash;

    public AuthService(UsersRepository usersRepository,
                       StudentsRepository studentsRepository,
                       AuthTokensRepository authTokensRepository,
                       PasswordEncoder passwordEncoder,
                       TokenTools tokenTools,
                       ApplicationEventPublisher eventPublisher,
                       @Value("${auth.password-reset-ttl}") Duration passwordResetTtl,
                       @Value("${auth.email-verification-ttl}") Duration emailVerificationTtl,
                       @Value("${auth.token-request-cooldown}") Duration tokenRequestCooldown) {
        this.usersRepository = usersRepository;
        this.studentsRepository = studentsRepository;
        this.authTokensRepository = authTokensRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenTools = tokenTools;
        this.eventPublisher = eventPublisher;
        this.passwordResetTtl = passwordResetTtl;
        this.emailVerificationTtl = emailVerificationTtl;
        this.tokenRequestCooldown = tokenRequestCooldown;
        this.dummyPasswordHash = passwordEncoder.encode(OpaqueTokens.generate());
    }

    //LOGIN
    @Override
    @Transactional(readOnly = true)
    public AuthResult login(LoginDTO body) {
        User user = usersRepository.findWithRolesByEmailNormalized(Emails.normalize(body.email())).orElse(null);

        String hash = user != null && user.getPasswordHash() != null ? user.getPasswordHash() : dummyPasswordHash;
        boolean passwordMatches = passwordEncoder.matches(body.password(), hash);

        if (user == null || user.getPasswordHash() == null || !passwordMatches
                || user.getStatus() == Status.suspended) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        log.info("Login riuscito: utente {}", user.getUserId());
        return new AuthResult(tokenTools.generateToken(user), toLoggedUserDTO(user));
    }

    @Override
    @Transactional(readOnly = true)
    public LoggedUserDTO getCurrentUser(Long userId) {
        User user = usersRepository.findWithRolesByUserId(userId)
                .orElseThrow(() -> new NotFoundException(userId));
        return toLoggedUserDTO(user);
    }

    //PASSWORD
    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(userId));
        if (user.getPasswordHash() == null) {
            throw new BadRequestException("L'account non ha una password locale: impostala con il recupero password");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("La password attuale non è corretta");
        }

        updatePassword(user, newPassword, OffsetDateTime.now());
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = usersRepository.findByEmailNormalized(Emails.normalize(email))
                .filter(found -> found.getStatus() != Status.suspended)
                .orElse(null);
        if (user == null) {
            log.info("Reset password ignorato: nessun account attivo per l'email indicata");
            return;
        }

        issueToken(user, TokenPurpose.password_reset, passwordResetTtl)
                .ifPresent(issued -> eventPublisher.publishEvent(
                        new PasswordResetRequested(user.getEmailNormalized(), issued.rawToken(), issued.expiresAt())));
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        OffsetDateTime now = OffsetDateTime.now();
        AuthToken authToken = consumeToken(token, TokenPurpose.password_reset, now);
        User user = authToken.getUser();

        // Chi apre il link ha accesso alla casella: l'email risulta verificata
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(now);
        }
        // TODO: i JWT già emessi restano validi fino alla scadenza (7 giorni);
        //  per invalidarli serve una versione del token o un password_changed_at verificato in TokenFilter
        updatePassword(user, newPassword, now);
    }

    //VERIFICA EMAIL
    @Override
    @Transactional
    public void requestEmailVerification(Long userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(userId));
        if (user.getEmailVerifiedAt() != null) {
            throw new BadRequestException("L'email è già verificata");
        }

        String name = user.getFirstName() != null ? user.getFirstName() : "";
        issueToken(user, TokenPurpose.email_verification, emailVerificationTtl)
                .ifPresent(issued -> eventPublisher.publishEvent(new EmailVerificationRequested(
                        user.getEmailNormalized(), name, issued.rawToken(), issued.expiresAt())));
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        OffsetDateTime now = OffsetDateTime.now();
        User user = consumeToken(token, TokenPurpose.email_verification, now).getUser();

        user.setEmailVerifiedAt(now);
        if (user.getStatus() == Status.pending_verification) {
            user.setStatus(Status.active);
        }
        log.info("Email verificata: utente {}", user.getUserId());
    }

    private void updatePassword(User user, String newPassword, OffsetDateTime now) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        // Eventuali link di reset ancora aperti non devono più funzionare
        authTokensRepository.revokeOpenTokens(user.getUserId(), TokenPurpose.password_reset, now);
        eventPublisher.publishEvent(new PasswordChanged(user.getEmailNormalized()));
        log.info("Password aggiornata: utente {}", user.getUserId());
    }

    // Nuovo token monouso: revoca i precedenti dello stesso scopo; vuoto se richiesto troppo presto
    private Optional<IssuedToken> issueToken(User user, TokenPurpose purpose, Duration ttl) {
        OffsetDateTime now = OffsetDateTime.now();
        if (authTokensRepository.existsByUserUserIdAndPurposeAndCreatedAtAfter(
                user.getUserId(), purpose, now.minus(tokenRequestCooldown))) {
            log.info("Richiesta {} ignorata per l'utente {}: troppo ravvicinata", purpose, user.getUserId());
            return Optional.empty();
        }

        authTokensRepository.revokeOpenTokens(user.getUserId(), purpose, now);
        String rawToken = OpaqueTokens.generate();
        OffsetDateTime expiresAt = now.plus(ttl);
        authTokensRepository.save(AuthToken.builder()
                .user(user)
                .tokenHash(OpaqueTokens.hash(rawToken))
                .purpose(purpose)
                .expiresAt(expiresAt)
                .build());
        return Optional.of(new IssuedToken(rawToken, expiresAt));
    }

    // Token dello scopo giusto, non consumato, non revocato e non scaduto; lo segna come consumato
    private AuthToken consumeToken(String rawToken, TokenPurpose purpose, OffsetDateTime now) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException(INVALID_LINK);
        }
        AuthToken authToken = authTokensRepository.findForUpdateByTokenHash(OpaqueTokens.hash(rawToken))
                .filter(found -> found.getPurpose() == purpose
                        && found.getConsumedAt() == null
                        && found.getRevokedAt() == null
                        && found.getExpiresAt().isAfter(now))
                .orElseThrow(() -> new BadRequestException(INVALID_LINK));
        authToken.setConsumedAt(now);
        return authToken;
    }

    private LoggedUserDTO toLoggedUserDTO(User user) {
        Student student = studentsRepository.findByUserUserId(user.getUserId()).orElse(null);
        Set<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toUnmodifiableSet());
        return new LoggedUserDTO(
                user.getUserId(),
                user.getEmailNormalized(),
                user.getFirstName(),
                user.getLastName(),
                roles,
                user.getEmailVerifiedAt() != null,
                user.getPasswordHash() != null,
                student != null ? student.getStudentId() : null,
                student != null ? student.getUsername() : null);
    }

    private record IssuedToken(String rawToken, OffsetDateTime expiresAt) {
    }
}
