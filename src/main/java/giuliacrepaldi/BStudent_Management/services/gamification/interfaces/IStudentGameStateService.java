package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CourseRewardsDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.DashboardGameDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.MissionRewardsDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.TopBarDTO;

/**
 * Dati di gioco per le pagine dello studente: barra fissa, dashboard, pagina del personaggio
 * (blueprint principale §10.5, §11.4; Blueprint Gamification §5, §6).
 * Compone i DTO chiamando gli altri service, senza regole proprie.
 *
 * <p>TODO (Fase E, frontend): nei tipi togliere SkinBonus/bonuses, ProfileTask.xpReward e bonusXp,
 * AppearanceCategory; aggiungere lo stato "riscattabile" agli accessori e il record alla Serie;
 * "Personaggio" al posto di "skin" in tutti i testi mostrati.
 * TODO (letture dello studente): IStudentCatalogService e IStudentMissionsService usano getCourseRewards e
 * getMissionRewards al posto della lista vuota di oggi.
 */
public interface IStudentGameStateService {

    TopBarDTO getTopBar(long studentId);

    DashboardGameDTO getDashboard(long studentId, long subjectId);

    CharacterPageDTO getCharacterPage(long studentId);

    // XP totali del corso e, se lo studente è ancora allo stadio Base, l'Evoluzione I (§6.4)
    CourseRewardsDTO getCourseRewards(long studentId, long courseId);

    // "+N XP" e punti skill massimi per skill (punti x quota della skill x fattore delle missioni)
    MissionRewardsDTO getMissionRewards(long missionId);
}
