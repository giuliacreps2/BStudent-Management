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

    //ESERCIZI DELLA MISSIONE: ognuno è un passo della sequenza, in coda al collegamento, nasce in bozza.
    // Body: { "questionBankEntryId": 5, "skills": [ { "skillId": 1, "weight": 100 } ] }, skills facoltative.
    // 409 se già usato nella missione o ritirato
    @PostMapping("/missions/{missionId}/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public MissionExerciseResponseDTO assign(@PathVariable long missionId, @RequestBody @Valid MissionExerciseDTO body) {
        return MissionExerciseResponseDTO.from(missionExercisesService.assignExercise(missionId, body));
    }

    // Nell'ordine della sequenza
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

    // Lo rende visibile allo studente in una missione già pubblicata (altrimenti esce con la missione).
    // 409 se l'esercizio non è pronto (pubblicato, con versione e skill) o la sequenza non resterebbe valida
    @PatchMapping("/mission-exercises/{missionExerciseId}/publish")
    public MissionExerciseResponseDTO publish(@PathVariable long missionExerciseId) {
        return MissionExerciseResponseDTO.from(missionExercisesService.publishExercise(missionExerciseId));
    }

    // Toglie anche il passo. 409 se già svolto o se una missione pubblicata resterebbe senza esercizi
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
