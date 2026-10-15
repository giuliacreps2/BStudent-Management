package giuliacrepaldi.BStudent_Management.enums.identity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum Relationship {
    parents("Genitore"),
    legal_guardian("Tutore Legale"),
    custodian("Affidatario"),
    relative("Altro Parente (Zio/Nonno)"),
    other("Altro / Nessuna relazione formale");


    private String description;

    public String getDescription() {
        return description;
    }
}
