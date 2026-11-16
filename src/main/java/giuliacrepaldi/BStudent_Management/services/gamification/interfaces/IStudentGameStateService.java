package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

/**
 * Dati di gioco per le pagine dello studente: barra fissa, dashboard, pagina del personaggio
 * (blueprint principale §10.5, §11.4; Blueprint Gamification §5, §6).
 * Solo lettura: compone i DTO chiamando gli altri service, senza regole proprie.
 * I DTO seguono i tipi del frontend (types/dashboard.ts, types/character.ts) con le modifiche decise.
 *
 * <h2>TODO — metodi da implementare (Fase D la struttura, Fase E il collegamento al frontend)</h2>
 * <pre>
 * TopBarDTO getTopBar(long studentId);
 *     // vite (saldo, base, ora della ricarica), XP e stadio, sfida del giorno, cariche del cappello
 *     // (rimaste e ora della ricarica, solo se posseduto), Serie. La materia attiva resta nel frontend
 * DashboardGameDTO getDashboard(long studentId, String subjectSlug);
 *     // personaggio (nome, immagine dello stadio), XP e cosa manca alla prossima evoluzione,
 *     // livelli delle skill della materia, Serie. Senza "bonuses" (§6.3) e senza XP del profilo (§6.5)
 * CharacterPageDTO getCharacterPage(long studentId);
 *     // roster (scelto / disponibile / "Prossimamente"), stadi con immagine solo se raggiunti,
 *     // accessori con stato (bloccato con soglia, riscattabile, posseduto, indossato). Senza "aspetto"
 * CourseRewardsDTO getCourseRewards(long studentId, long courseId);
 *     // XP totali del corso e, se lo studente non ha ancora completato un corso, l'Evoluzione I (§6.4).
 *     // Lo usa IStudentCatalogService (branch dei corsi) al posto della lista vuota di oggi
 * MissionRewardsDTO getMissionRewards(long missionId);
 *     // "+N XP" e punti skill massimi per skill (punti x quota della skill x fattore delle missioni)
 * </pre>
 * Modifiche ai tipi del frontend (Fase E): togliere SkinBonus/bonuses, ProfileTask.xpReward e bonusXp,
 * AppearanceCategory; stato "riscattabile" negli accessori; StatsData.streak con il record;
 * "Personaggio" al posto di "skin" in tutti i testi mostrati.
 * Controller: GET /student/game/top-bar, /student/game/dashboard, /student/character.
 */
public interface IStudentGameStateService {
}
