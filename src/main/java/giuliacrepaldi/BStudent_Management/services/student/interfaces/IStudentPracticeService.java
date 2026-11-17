package giuliacrepaldi.BStudent_Management.services.student.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.student.ExercisesPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeListDTO;

import java.util.List;

/**
 * Palestra vista dallo studente, "Esercizi" nel menu (blueprint §7.3; frontend types/exercises.ts).
 * Solo voci della Palestra (practice_exercises): gli id mandati al frontend sono quelli delle voci.
 * Gli esercizi e i tentativi delle missioni non compaiono qui. Lo svolgimento è del Blocco 5.
 *
 * <p>Ogni voce ha due lucchetti distinti: difficoltà (livello della skill, Blueprint Gamification §3) e premium.
 * TODO (programma, §3-bis): "Per la tua classe" e "Più avanti nel programma" quando esisterà la classe dello
 * studente; oggi il frontend riceve solo la classe di riferimento dell'argomento.
 */
public interface IStudentPracticeService {

    // subjectSlug null o non valido: la prima materia pubblicata con voci visibili
    ExercisesPageDTO getExercisesPage(long studentId, String subjectSlug);

    // Voci visibili della skill, raggruppate per argomento
    PracticeListDTO getPracticeBySkill(long studentId, String subjectSlug, String skillCode);

    // "Obiettivo di oggi": skillCode null = "Sorprendimi" (la skill più debole). 3 voci: prima da rifare, poi
    // mai svolte, poi le altre; escluse le bloccate (difficoltà o premium)
    List<PracticeItemDTO> getTodayGoal(long studentId, String subjectSlug, String skillCode);

    // Le voci indicate che lo studente vede, nell'ordine dato: per i consigli del test di orientamento
    List<PracticeItemDTO> getByIds(long studentId, List<Long> practiceExerciseIds);

    // "Vai agli esercizi" dalle Risorse; vuota = pulsante nascosto
    List<PracticeItemDTO> getByTopic(long studentId, long topicId);
}
