package giuliacrepaldi.BStudent_Management.enums.identity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum Relationship {
    GENITORE("Genitore"),
    TUTORELEGALE("Tutore Legale"),
    AFFIDATARIO("Affidatario"),
    PARENTE("Altro Parente (Zio/Nonno)"),
    ALTRO("Altro / Nessuna relazione formale");


    private String description;

    public String getDescription() {
        return description;
    }
}
