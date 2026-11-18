package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;

// Risposta al salvataggio automatico: stato della missione e tempo rimasto.
// active = tutto bene, il tempo scorre; saved = la missione è andata in pausa perché il collegamento si era
// interrotto (mostrare "Riprendi"); failed = tempo finito, tentativo fallito (ricaricare lo stato della missione)
public record MissionSyncDTO(MissionAttemptStatus status, long remainingMs) {
}
