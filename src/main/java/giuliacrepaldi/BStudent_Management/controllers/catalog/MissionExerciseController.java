package giuliacrepaldi.BStudent_Management.controllers.catalog;

import giuliacrepaldi.BStudent_Management.payloads.exercises.MissionExerciseDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.MissionExerciseResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.SkillWeightResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.SkillWeightsDTO;
import giuliacrepaldi.BStudent_Management.services.exercises.interfaces.IMissionExercisesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**
@RestController
@RequestMapping("/admin/academy")
public class MissionExerciseController {

    private final IMissionExercisesService missionExercisesService;

    public MissionExerciseController(IMissionExercisesService missionExercisesService) {
        this.missionExercisesService = missionExercisesService;
    }

    //ESERCIZIO DELLA LEZIONE: uno per lezione
    @PostMapping("/lessons/{lessonId}/exercise")
    @ResponseStatus(HttpStatus.CREATED)
    public MissionExerciseResponseDTO assign(@PathVariable long lessonId, @RequestBody @Valid MissionExerciseDTO body) {
        return MissionExerciseResponseDTO.from(missionExercisesService.assignExercise(lessonId, body));
    }

    // 404 se la lezione non ha un esercizio collegato
    @GetMapping("/lessons/{lessonId}/exercise")
    public MissionExerciseResponseDTO findByLesson(@PathVariable long lessonId) {
        return MissionExerciseResponseDTO.from(missionExercisesService.findByLesson(lessonId));
    }

    // Nell'ordine delle lezioni
    @GetMapping("/missions/{missionId}/exercises")
    public List<MissionExerciseResponseDTO> findByMission(@PathVariable long missionId) {
        return missionExercisesService.findByMission(missionId).stream().map(MissionExerciseResponseDTO::from).toList();
    }

    //COLLEGAMENTI
    @GetMapping("/mission-exercises/{missionExerciseId}")
    public MissionExerciseResponseDTO findById(@PathVariable long missionExerciseId) {
        return MissionExerciseResponseDTO.from(missionExercisesService.findById(missionExerciseId));
    }

    // Tempo e punti; campi null invariati
    @PutMapping("/mission-exercises/{missionExerciseId}")
    public MissionExerciseResponseDTO update(@PathVariable long missionExerciseId,
                                             @RequestBody @Valid MissionExerciseDTO body) {
        return MissionExerciseResponseDTO.from(missionExercisesService.findByIdAndUpdate(missionExerciseId, body));
    }

    // 409 se già svolto o se la lezione è pubblicata
    @DeleteMapping("/mission-exercises/{missionExerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable long missionExerciseId) {
        missionExercisesService.removeExercise(missionExerciseId);
    }

    //PESI SKILL: lista vuota = 100% sulla skill predefinita della materia
    @GetMapping("/mission-exercises/{missionExerciseId}/skills")
    public List<SkillWeightResponseDTO> findSkillWeights(@PathVariable long missionExerciseId) {
        return missionExercisesService.findSkillWeights(missionExerciseId).stream().map(SkillWeightResponseDTO::from).toList();
    }

    // Body: { "weights": [ { "skillId": 1, "weight": 70 }, { "skillId": 2, "weight": 30 } ] }
    @PutMapping("/mission-exercises/{missionExerciseId}/skills")
    public List<SkillWeightResponseDTO> replaceSkillWeights(@PathVariable long missionExerciseId,
                                                            @RequestBody @Valid SkillWeightsDTO body) {
        return missionExercisesService.replaceSkillWeights(missionExerciseId, body.weights()).stream()
                .map(SkillWeightResponseDTO::from).toList();
    }
}
