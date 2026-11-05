package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Il corso arriva dal path alla creazione; ordine e stato cambiano solo con reorder e publish/withdraw
public record MissionDTO(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String shortDescription,
        // null = eredita dal corso
        AccessTier accessTier) {
}
