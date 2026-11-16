package giuliacrepaldi.BStudent_Management.payloads.catalog;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Esattamente un contenuto: testo, immagine, oppure video (provider + identificativo).
// Il tipo della pagina si ricava da quale campo è valorizzato; la coerenza la verifica il service
public record LessonPageDTO(
        // TODO formato deciso il 14/11/2026 (blueprint §7.4): blocchi come gli articoli del Blog (titolo, paragrafo,
        // citazione, riquadro, immagine, elenco; grassetto e corsivo minimi nei paragrafi), uguale per Risorse e Blog.
        // Da fare (4.4, con l'editor): struttura JSON validata con ContentBlocks.validateBlocks al posto del testo libero
        String formattedText,
        // Identificativo Cloudinary (blueprint §5.1). TODO: caricamento dall'Admin tramite il backend
        @Size(max = 500) String imageReference,
        // youtube (non in elenco) per i contenuti free, vimeo per i premium (deciso il 16/11/2026, §5.1)
        @Pattern(regexp = "youtube|vimeo", message = "Piattaforma video non valida: YouTube o Vimeo")
        String videoProvider,
        @Size(max = 200) String videoIdentifier) {
}
