package giuliacrepaldi.BStudent_Management.payloads.catalog;

import jakarta.validation.constraints.Size;

// Esattamente un contenuto: testo, immagine, oppure video (provider + identificativo).
// Il tipo della pagina si ricava da quale campo è valorizzato; la coerenza la verifica il service
public record LessonPageDTO(
        // TODO formato del testo (es. Markdown) e sanificazione al rendering da decidere col frontend
        String formattedText,
        @Size(max = 500) String imageReference,
        // TODO provider ammessi (es. youtube, vimeo) da decidere; oggi testo libero
        @Size(max = 40) String videoProvider,
        @Size(max = 200) String videoIdentifier) {
}
