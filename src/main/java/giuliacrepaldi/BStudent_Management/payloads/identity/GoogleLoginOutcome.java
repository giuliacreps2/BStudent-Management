package giuliacrepaldi.BStudent_Management.payloads.identity;

// Esito del ritorno da Google: il success handler sceglie cookie e redirect con uno switch esaustivo
public sealed interface GoogleLoginOutcome {

    // Account esistente (identità già collegata o collegata ora tramite email verificata)
    record LoggedIn(AuthResult auth) implements GoogleLoginOutcome {
    }

    // Nuovo utente: servono data di nascita, username e informativa. Il token va nel cookie di onboarding
    record OnboardingRequired(String onboardingToken) implements GoogleLoginOutcome {

        @Override
        public String toString() {
            return "OnboardingRequired[]";
        }
    }

    // reason è un codice stabile, passato al frontend come ?error=
    record Rejected(String reason) implements GoogleLoginOutcome {
    }
}
