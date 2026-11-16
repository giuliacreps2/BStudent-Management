package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

/**
 * Risultato del corso e prima evoluzione (blueprint §10.4).
 * Media dei migliori risultati delle missioni; completamento con tutte le missioni superate.
 * Prima evoluzione v0.1 una sola volta: base vite da 5 a 7, saldo portato almeno a 7.
 * Deciso il 16/11/2026 (Blueprint Gamification §8-bis): "completato" resta "tutte le missioni superate",
 * senza completamento ridotto per chi non paga. Ogni materia ha un corso d'ingresso tutto gratuito
 * (courses.is_entry_course), che dà la prima evoluzione a chiunque.
 * TODO: l'Admin dei corsi e delle missioni non deve permettere missioni premium in un corso d'ingresso,
 * né rendere premium il corso (ICoursesService, IMissionsService).
 */
public interface ICourseProgressService {
}
