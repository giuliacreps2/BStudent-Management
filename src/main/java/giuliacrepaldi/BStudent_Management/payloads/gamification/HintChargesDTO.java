package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.time.OffsetDateTime;

// Cariche dell'aiuto del cappello (📜). owned = false: lo studente non ha ancora un accessorio con l'aiuto.
// rechargeAt = quando tornano tutte le cariche; null se non c'è una ricarica in corso
public record HintChargesDTO(boolean owned, boolean equipped, String accessoryName, int remaining, int capacity,
                             OffsetDateTime rechargeAt) {

    public static HintChargesDTO none() {
        return new HintChargesDTO(false, false, null, 0, 0, null);
    }
}
