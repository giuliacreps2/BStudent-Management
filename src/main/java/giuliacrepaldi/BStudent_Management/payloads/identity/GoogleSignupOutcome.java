package giuliacrepaldi.BStudent_Management.payloads.identity;

public sealed interface GoogleSignupOutcome {

    // Dai 14 anni: account creato subito e studente autenticato
    record Created(AuthResult auth) implements GoogleSignupOutcome {
    }

    // Sotto i 14 anni: richiesta salvata e link di consenso inviato al genitore/tutore
    record AwaitingGuardianConsent() implements GoogleSignupOutcome {
    }
}
