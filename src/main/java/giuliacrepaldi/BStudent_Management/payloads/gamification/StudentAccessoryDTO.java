package giuliacrepaldi.BStudent_Management.payloads.gamification;

import giuliacrepaldi.BStudent_Management.enums.gamification.AccessoryStatus;

import java.math.BigDecimal;

// Accessorio visto dallo studente (blueprint §11.1). status: locked, redeemable, owned, equipped.
// currentPoints = punti veri nella skill richiesta, nella materia migliore. unlockHint solo se bloccato.
// benefitDescription e charges solo se l'accessorio dà un aiuto. L'immagine sta nel frontend, per code
public record StudentAccessoryDTO(Long accessoryId, String code, String name, String slotCode, AccessoryStatus status,
                                  String skillName, BigDecimal requiredPoints, BigDecimal currentPoints,
                                  String unlockHint, String benefitDescription, HintChargesDTO charges) {
}
