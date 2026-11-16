package giuliacrepaldi.BStudent_Management.payloads.catalog;

import jakarta.validation.constraints.Size;

// Esattamente un contenuto: testo, immagine, oppure video (provider + identificativo).
// Il tipo della pagina si ricava da quale campo è valorizzato; la coerenza la verifica il service
public record LessonPageDTO(
        // TODO formato deciso il 14/11/2026 (blueprint §7.4): blocchi come gli articoli del Blog (titolo, paragrafo,
        // citazione, riquadro, immagine, elenco; grassetto e corsivo minimi nei paragrafi), uguale per Risorse e Blog.
        // Da fare: struttura JSON validata dal backend al posto del testo libero e sanificazione al rendering
        String formattedText,
        @Size(max = 500) String imageReference,
        // TODO provider ammessi (es. youtube, vimeo) da decidere; oggi testo libero
        @Size(max = 40) String videoProvider,
        @Size(max = 200) String videoIdentifier) {
}
