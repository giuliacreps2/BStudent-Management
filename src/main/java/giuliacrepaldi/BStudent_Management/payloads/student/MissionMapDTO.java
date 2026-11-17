package giuliacrepaldi.BStudent_Management.payloads.student;

import java.util.List;

// Mappa del corso (blueprint §5.9): tema del mondo e citazione del corso, missioni visibili nell'ordine
public record MissionMapDTO(String courseSlug, String courseTitle, String subjectSlug, String subjectName,
                            String worldTheme, String quote, String quoteAuthor, List<MissionNodeDTO> missions) {
}
