package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.payloads.gamification.DailyChallengeDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeRunStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StreakDTO;

import java.util.List;

// Pagina "Esercizi", cioè la Palestra (blueprint §7.3): materie, materia attiva, skill nell'ordine dell'autrice,
// attività da riprendere, sfida del giorno, cammino verso la prossima vita, Serie.
// hasOrientationTest = mostrare il link "Non sai cosa allenare? Fai il test". Nessun bonus
public record ExercisesPageDTO(List<SubjectCardDTO> subjects, SubjectCardDTO activeSubject, List<PracticeSkillDTO> skills,
                               List<PracticeItemDTO> resumeActivities, DailyChallengeDTO dailyChallenge,
                               PracticeRunStateDTO practiceRun, StreakDTO streak) {
}
