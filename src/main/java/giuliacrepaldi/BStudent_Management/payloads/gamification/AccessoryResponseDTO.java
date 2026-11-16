package giuliacrepaldi.BStudent_Management.payloads.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.Accessory;
import giuliacrepaldi.BStudent_Management.entities.gamification.AccessoryBenefit;

import java.math.BigDecimal;

// Vista dell'Admin. owned = almeno uno studente lo possiede
public record AccessoryResponseDTO(Long accessoryId, String code, String name, String slotCode, Long skillId,
                                   String skillName, BigDecimal requiredPoints, boolean available,
                                   Integer hintCapacity, Integer hintRechargeHours, boolean owned) {

    public static AccessoryResponseDTO from(Accessory accessory, AccessoryBenefit benefit, boolean owned) {
        return new AccessoryResponseDTO(accessory.getAccessoryId(), accessory.getAccessoryCode(),
                accessory.getAccessoryName(), accessory.getAccessorySlotCode(), accessory.getSkill().getSkillId(),
                accessory.getSkill().getSkillName(), accessory.getRequiredPoints(), accessory.isAvailable(),
                benefit == null ? null : benefit.getCapacity(), benefit == null ? null : benefit.getRechargeHours(), owned);
    }
}
