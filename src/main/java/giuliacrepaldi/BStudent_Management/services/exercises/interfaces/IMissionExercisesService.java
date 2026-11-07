package giuliacrepaldi.BStudent_Management.services.exercises.interfaces;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;
import giuliacrepaldi.BStudent_Management.payloads.exercises.MissionExerciseDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.SkillWeightDTO;

import java.util.List;

/**
 * Esercizio collegato alla missione (blueprint §5.1): è un passo della sua sequenza (v0.8), non appartiene a una lezione.
 * Lo stesso esercizio non è duplicato nella stessa missione.
 * Stato draft/published: nasce draft; in una missione pubblicata resta nascosto finché non si pubblica (publishExercise), altrimenti viene pubblicato insieme alla missione.
 * È un collegamento, non un contenuto: niente ritiro, si ritira l'esercizio nel banco o lo si scollega.
 * <p>
 * findSkillWeights e replaceSkillWeights: sostituisce tutti i pesi insieme. La somma deve fare 100; una lista vuota vuol dire 100% sulla skill predefinita della materia.
 * TODO (gamification, blueprint §10.3): le regole delle skill non sono ancora chiare. Ricontrollare pesi, fallback
 * sulla materia e requisito delle skill per pubblicare (MissionsService.exerciseProblems) quando si sviluppa la progressione.
 * TODO da ricontrollare removeExercise
 * - removeExercise: solo se l'esercizio non è mai stato svolto e, in una missione pubblicata, se la sequenza resta valida.
 */
public interface IMissionExercisesService {

    //Admin

    // Nasce draft, in coda alla sequenza; skill opzionali. Tempo e punti mancanti nel body -> default di academy_settings
    MissionExercise assignExercise(long missionId, MissionExerciseDTO body);

    MissionExercise findById(long missionExerciseId);

    List<MissionExercise> findByMission(long missionId); // nell'ordine della sequenza

    MissionExercise findByIdAndUpdate(long missionExerciseId, MissionExerciseDTO body); // tempo e punti

    List<MissionExerciseSkill> findSkillWeights(long missionExerciseId);

    // Sostituisce tutti i pesi: somma 100, oppure lista vuota = 100% sulla skill predefinita della materia
    List<MissionExerciseSkill> replaceSkillWeights(long missionExerciseId, List<SkillWeightDTO> weights);

    // draft -> published: esercizio pronto (pubblicato, con versione e skill) e, in una missione pubblicata, sequenza valida
    MissionExercise publishExercise(long missionExerciseId);

    void removeExercise(long missionExerciseId); // solo se mai svolto, altrimenti ConflictException
}
