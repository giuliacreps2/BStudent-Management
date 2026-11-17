package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.payloads.gamification.CourseRewardsDTO;

import java.util.List;

// Pagina del corso (blueprint §5.8): sottotitolo, descrizione, citazione, skill allenate e ricompense vere
// (XP totali e, per chi è allo stadio Base, l'Evoluzione I: Blueprint Gamification §6.4).
// Il riepilogo del Quaderno arriva con il Quaderno
public record CourseDetailDTO(CourseCardDTO course, SubjectCardDTO subject, String tagline, String description,
                              String quote, String quoteAuthor, List<SkillRefDTO> skills, CourseRewardsDTO rewards) {
}
