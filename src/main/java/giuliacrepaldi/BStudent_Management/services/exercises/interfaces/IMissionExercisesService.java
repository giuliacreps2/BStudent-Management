package giuliacrepaldi.BStudent_Management.services.exercises.interfaces;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;
import giuliacrepaldi.BStudent_Management.payloads.exercises.MissionExerciseDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.SkillWeightDTO;

import java.util.List;

/**
 * Esercizio collegato alla lezione (blueprint §5.1).
 * Al massimo un esercizio per lezione; lo stesso esercizio non è duplicato nella stessa missione.
 * È un collegamento, non un contenuto: nessuno stato proprio.
 * <p>
 * findSkillWeights e replaceSkillWeights: sostituisce tutti i pesi insieme. La somma deve fare 100; una lista vuota vuol dire 100% sulla skill predefinita della materia.
 * TODO da ricontrollare removeExercise
 * - removeExercise: solo se l'esercizio non è mai stato svolto.
 */
public interface IMissionExercisesService {

    //Admin

    // Tempo e punti mancanti nel body -> default di academy_settings
    MissionExercise assignExercise(long lessonId, MissionExerciseDTO body);

    MissionExercise findById(long missionExerciseId);

    MissionExercise findByLesson(long lessonId);

    List<MissionExercise> findByMission(long missionId); // nell'ordine delle lezioni

    MissionExercise findByIdAndUpdate(long missionExerciseId, MissionExerciseDTO body); // tempo e punti

    List<MissionExerciseSkill> findSkillWeights(long missionExerciseId);

    // Sostituisce tutti i pesi: somma 100, oppure lista vuota = 100% sulla skill predefinita della materia
    List<MissionExerciseSkill> replaceSkillWeights(long missionExerciseId, List<SkillWeightDTO> weights);

    void removeExercise(long missionExerciseId); // solo se mai svolto, altrimenti ConflictException
}
