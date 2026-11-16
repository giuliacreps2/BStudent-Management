package giuliacrepaldi.BStudent_Management.services.student.interfaces;

/**
 * Mappa del corso e pagina della missione viste dallo studente (blueprint §5.9; frontend types/missions.ts).
 * Lo svolgimento (avvio, tentativi, timer, checkpoint) è del Blocco 5, non di qui.
 *
 * <h2>TODO — metodi da implementare (Fase 3)</h2>
 * <pre>
 * MissionMapDTO getMissionMap(long studentId, String courseSlug);
 *     // tema del mondo e citazione del corso; missioni visibili nell'ordine, ognuna con slug, titolo,
 *     // tagline, copertina, prova finale, stato calcolato dall'ordine (superate = completate, la prima non
 *     // superata = attuale, le altre bloccate; fino al Blocco 5 la prima è attuale) e lucchetto premium.
 *     // Le posizioni sulla mappa le assegna il frontend secondo il tema
 * MissionPageDTO getMissionPage(long studentId, String courseSlug, String missionSlug);
 *     // tagline, "Cosa imparerai", copertina; durata = somma dei tempi degli esercizi visibili; soglia 60%
 *     // (academy_settings) al posto del massimo errori; vite dello studente (serve almeno una vita);
 *     // "+N XP" = somma dei punti massimi degli esercizi e punti skill massimi per skill
 *     // (punti x peso x fattore missioni); stepper = sequenza visibile (lezione/esercizio con titolo per lo
 *     // studente) + "Ricompensa"; briefing = guida della materia (nome, immagine), battute, video.
 *     // Contatori: vite e cariche del cappello (gamification: fino ad allora valori dello stato iniziale).
 *     // Classifica: non restituita (fuori MVP)
 * boolean isTutorialSeen(long studentId);
 * void markTutorialSeen(long studentId);
 *     // tutorial della prima volta mostrato una volta sola (students.tutorial_seen_at); idempotente
 * </pre>
 * Campo da mappare: Student.tutorialSeenAt. Controller: /student/courses/{courseSlug}/missions/**, /student/tutorial.
 */
public interface IStudentMissionsService {
}
