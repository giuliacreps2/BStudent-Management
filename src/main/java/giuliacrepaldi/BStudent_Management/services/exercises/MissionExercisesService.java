package giuliacrepaldi.BStudent_Management.services.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;
import giuliacrepaldi.BStudent_Management.payloads.exercises.MissionExerciseDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.SkillWeightDTO;
import giuliacrepaldi.BStudent_Management.services.exercises.interfaces.IMissionExercisesService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MissionExercisesService implements IMissionExercisesService {

    @Override
    public MissionExercise assignExercise(long lessonId, MissionExerciseDTO body) {
        return null;
    }

    @Override
    public MissionExercise findById(long missionExerciseId) {
        return null;
    }

    @Override
    public MissionExercise findByLesson(long lessonId) {
        return null;
    }

    @Override
    public List<MissionExercise> findByMission(long missionId) {
        return List.of();
    }

    @Override
    public MissionExercise findByIdAndUpdate(long missionExerciseId, MissionExerciseDTO body) {
        return null;
    }

    @Override
    public List<MissionExerciseSkill> findSkillWeights(long missionExerciseId) {
        return List.of();
    }

    @Override
    public List<MissionExerciseSkill> replaceSkillWeights(long missionExerciseId, List<SkillWeightDTO> weights) {
        return List.of();
    }

    @Override
    public void removeExercise(long missionExerciseId) {

    }
}
