package giuliacrepaldi.BStudent_Management.services.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.entities.identity.UserIdentity;
import giuliacrepaldi.BStudent_Management.enums.identity.AuthProvider;
import giuliacrepaldi.BStudent_Management.enums.identity.RegistrationRequestStatus;
import giuliacrepaldi.BStudent_Management.enums.identity.Status;
import giuliacrepaldi.BStudent_Management.payloads.identity.*;
import giuliacrepaldi.BStudent_Management.repositories.identity.RegistrationRequestsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.UserIdentitiesRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.UsersRepository;
import giuliacrepaldi.BStudent_Management.security.TokenTools;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IAuthService;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IGoogleAuthService;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IRegistrationService;
import giuliacrepaldi.BStudent_Management.utilities.Emails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
public class GoogleAuthService implements IGoogleAuthService {

    // Codici stabili passati al frontend come /login?error=<codice>
    static final String EMAIL_UNVERIFIED = "google_email_unverified";
    static final String ACCOUNT_SUSPENDED = "account_suspended";
    static final String ACCOUNT_UNAVAILABLE = "account_unavailable";
    static final String GOOGLE_ACCOUNT_MISMATCH = "google_account_mismatch";
    static final String AWAITING_GUARDIAN_CONSENT = "awaiting_guardian_consent";

    private final UserIdentitiesRepository userIdentitiesRepository;
    private final UsersRepository usersRepository;
    private final RegistrationRequestsRepository registrationRequestsRepository;
    private final IRegistrationService registrationService;
    private final IAuthService authService;
    private final TokenTools tokenTools;

    public GoogleAuthService(UserIdentitiesRepository userIdentitiesRepository,
                             UsersRepository usersRepository,
                             RegistrationRequestsRepository registrationRequestsRepository,
                             IRegistrationService registrationService,
                             IAuthService authService,
                             TokenTools tokenTools) {
        this.userIdentitiesRepository = userIdentitiesRepository;
        this.usersRepository = usersRepository;
        this.registrationRequestsRepository = registrationRequestsRepository;
        this.registrationService = registrationService;
        this.authService = authService;
        this.tokenTools = tokenTools;
    }

    //LOGIN
    @Override
    @Transactional
    public GoogleLoginOutcome handleGoogleLogin(GoogleProfile profile) {
        if (!profile.emailVerified() || profile.email() == null) {
            return new GoogleLoginOutcome.Rejected(EMAIL_UNVERIFIED);
        }

        // 1. Identità già collegata: login, anche se l'email su Google è cambiata nel frattempo
        UserIdentity identity = userIdentitiesRepository
                .findByProviderAndProviderSubject(AuthProvider.google, profile.subject()).orElse(null);
        if (identity != null) {
            // Utente cancellato (soft delete): l'identità resta ma l'account non è utilizzabile
            return identity.getUser() == null
                    ? new GoogleLoginOutcome.Rejected(ACCOUNT_UNAVAILABLE)
                    : loginOutcome(identity.getUser());
        }

        // 2. Account esistente con la stessa email: collegamento automatico, l'email è verificata da Google
        String email = Emails.normalize(profile.email());
        User existing = usersRepository.findWithRolesByEmailNormalized(email).orElse(null);
        if (existing != null) {
            if (existing.getStatus() == Status.suspended) {
                return new GoogleLoginOutcome.Rejected(ACCOUNT_SUSPENDED);
            }
            // L'account è già collegato a un altro profilo Google: nessuna sostituzione silenziosa
            if (userIdentitiesRepository.existsByUserUserIdAndProvider(existing.getUserId(), AuthProvider.google)) {
                return new GoogleLoginOutcome.Rejected(GOOGLE_ACCOUNT_MISMATCH);
            }
            linkGoogleIdentity(existing, profile.subject(), email);
            return loginOutcome(existing);
        }
        if (usersRepository.existsByEmailIncludingDeleted(email)) {
            return new GoogleLoginOutcome.Rejected(ACCOUNT_UNAVAILABLE);
        }

        // 3. Minore già registrato con Google, in attesa del consenso del genitore
        if (registrationRequestsRepository.existsByGoogleSubjectAndStatusAndExpiresAtAfter(
                profile.subject(), RegistrationRequestStatus.pending, OffsetDateTime.now())) {
            return new GoogleLoginOutcome.Rejected(AWAITING_GUARDIAN_CONSENT);
        }

        // 4. Nuovo utente: niente salvato nel DB finché non completa l'onboarding
        return new GoogleLoginOutcome.OnboardingRequired(tokenTools.generateGoogleOnboardingToken(profile));
    }

    @Override
    public GoogleOnboardingDTO onboardingProfile(String onboardingToken) {
        GoogleProfile profile = tokenTools.parseGoogleOnboardingToken(onboardingToken);
        return new GoogleOnboardingDTO(profile.email(), profile.givenName(), profile.familyName());
    }

    //REGISTRAZIONE
    @Override
    public GoogleSignupOutcome completeGoogleSignup(String onboardingToken, GoogleSignupDTO body) {
        GoogleProfile profile = tokenTools.parseGoogleOnboardingToken(onboardingToken);
        return registrationService.registerWithGoogle(profile, body)
                .<GoogleSignupOutcome>map(user -> new GoogleSignupOutcome.Created(authResultOf(user)))
                .orElseGet(GoogleSignupOutcome.AwaitingGuardianConsent::new);
    }

    private GoogleLoginOutcome loginOutcome(User user) {
        if (user.getStatus() == Status.suspended) {
            return new GoogleLoginOutcome.Rejected(ACCOUNT_SUSPENDED);
        }
        log.info("Login con Google riuscito: utente {}", user.getUserId());
        return new GoogleLoginOutcome.LoggedIn(authResultOf(user));
    }

    private void linkGoogleIdentity(User user, String subject, String email) {
        userIdentitiesRepository.save(UserIdentity.builder()
                .user(user)
                .provider(AuthProvider.google)
                .providerSubject(subject)
                .emailAtLink(email)
                .build());
        // Google ha verificato che l'utente possiede questa casella (es. minore dopo il consenso)
        if (user.getEmailVerifiedAt() == null) {
            user.setEmailVerifiedAt(OffsetDateTime.now());
        }
        log.info("Identità Google collegata all'utente {}", user.getUserId());
    }

    private AuthResult authResultOf(User user) {
        return new AuthResult(tokenTools.generateToken(user), authService.getCurrentUser(user.getUserId()));
    }
}
