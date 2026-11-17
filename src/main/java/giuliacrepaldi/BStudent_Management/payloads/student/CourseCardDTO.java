package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;

// Corso nell'elenco dello studente (blueprint §5.8). stepsCount = missioni visibili; skillsCount = skill diverse
// degli esercizi visibili. progress: percentuale di missioni superate, null = non ancora iniziato.
// featured = "In evidenza" (lo decide l'Admin, fino a una data); popular = calcolato dall'uso, false fino
// al Blocco 5. premiumLocked = corso a pagamento e studente senza accesso: il lucchetto è distinto dallo stato.
// entryCourse = corso d'ingresso gratuito della materia. L'indirizzo lo costruisce il frontend dallo slug
public record CourseCardDTO(String slug, String subjectSlug, String title, String description, String imageReference,
                            int stepsCount, CourseLevel level, int skillsCount, Integer progress, boolean featured,
                            boolean popular, boolean premiumLocked, boolean entryCourse) {
}
