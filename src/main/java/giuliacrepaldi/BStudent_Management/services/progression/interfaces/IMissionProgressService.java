package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.progression.MissionPassedDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.SkillPointsDTO;

/**
 * Progressione delle missioni (blueprint principale §9.3, §10.3; Blueprint Svolgimento §5).
 * Risultato dell'esercizio: una percentuale, ripartita fra le skill secondo le quote dell'esercizio.
 * Punti skill con il fattore delle missioni (10%), conservati anche se la missione non è superata; rifare un
 * esercizio accredita solo la differenza rispetto al proprio risultato migliore, skill per skill.
 * XP solo al primo superamento della missione. I calcoli sono in utilities.PlayRules (coperti dai test).
 */
public interface IMissionProgressService {

    // Dopo ogni giudizio di un esercizio di missione, superato o no (automatico o della docente): per ogni skill
    // dell'esercizio accredita la differenza positiva rispetto al migliore contributo già ottenuto in quell'esercizio
    // (origine "mission", materia del corso) e aggiorna mission_skill_contributions; poi le evoluzioni.
    // Ripetuto per lo stesso tentativo: nessun doppio accredito
    SkillPointsDTO onExerciseAssessed(long studentId, long exerciseAttemptId);

    // Percorso superato: mission_records con il risultato migliore (media degli esercizi superati nel percorso,
    // pesata sui punti; un risultato peggiore in una ripetizione non lo abbassa); XP al primo superamento;
    // poi il corso (ICourseProgressService.onMissionPassed)
    MissionPassedDTO onMissionPassed(long studentId, long missionAttemptId);
}
