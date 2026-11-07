package giuliacrepaldi.BStudent_Management.controllers.catalog;

import giuliacrepaldi.BStudent_Management.payloads.catalog.*;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.IMissionsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**
@RestController
@RequestMapping("/admin/academy")
public class MissionController {

    private final IMissionsService missionsService;

    public MissionController(IMissionsService missionsService) {
        this.missionsService = missionsService;
    }

    //MISSIONI DEL CORSO: il corso è nel path, la missione nasce draft in coda all'ordine
    @PostMapping("/courses/{courseId}/missions")
    @ResponseStatus(HttpStatus.CREATED)
    public MissionResponseDTO create(@PathVariable long courseId, @RequestBody @Valid MissionDTO body) {
        return MissionResponseDTO.from(missionsService.saveNewMission(courseId, body));
    }

    // In ordine, tutti gli stati: è la vista dell'Admin
    @GetMapping("/courses/{courseId}/missions")
    public List<MissionResponseDTO> findByCourse(@PathVariable long courseId) {
        return missionsService.findByCourse(courseId).stream().map(MissionResponseDTO::from).toList();
    }

    // Body: { "ids": [...] } con tutti e soli gli ID delle missioni del corso, nel nuovo ordine
    @PutMapping("/courses/{courseId}/missions/order")
    public List<MissionResponseDTO> reorder(@PathVariable long courseId, @RequestBody @Valid ReorderDTO body) {
        return missionsService.reorderMissions(courseId, body.ids()).stream().map(MissionResponseDTO::from).toList();
    }

    //MISSIONI
    @GetMapping("/missions/{missionId}")
    public MissionResponseDTO findById(@PathVariable long missionId) {
        return MissionResponseDTO.from(missionsService.findById(missionId));
    }

    @PutMapping("/missions/{missionId}")
    public MissionResponseDTO update(@PathVariable long missionId, @RequestBody @Valid MissionDTO body) {
        return MissionResponseDTO.from(missionsService.findByIdAndUpdate(missionId, body));
    }

    //SEQUENZA: lezioni ed esercizi in un unico ordine
    @GetMapping("/missions/{missionId}/steps")
    public List<MissionStepResponseDTO> findSteps(@PathVariable long missionId) {
        return missionsService.findSteps(missionId).stream().map(MissionStepResponseDTO::from).toList();
    }

    // Body: { "ids": [...] } con tutti e soli gli ID dei passi della missione, nel nuovo ordine.
    // 409 se la missione è pubblicata e la nuova sequenza non è valida
    @PutMapping("/missions/{missionId}/steps/order")
    public List<MissionStepResponseDTO> reorderSteps(@PathVariable long missionId, @RequestBody @Valid ReorderDTO body) {
        return missionsService.reorderSteps(missionId, body.ids()).stream().map(MissionStepResponseDTO::from).toList();
    }

    // Da mostrare prima di pubblicare: i problemi bloccano, gli avvisi no
    @GetMapping("/missions/{missionId}/readiness")
    public MissionReadinessDTO checkReadiness(@PathVariable long missionId) {
        return missionsService.checkReadiness(missionId);
    }

    //STATO: cambi di stato senza body; 409 se la transizione non è ammessa o la missione non è pronta
    @PatchMapping("/missions/{missionId}/publish")
    public MissionResponseDTO publish(@PathVariable long missionId) {
        return MissionResponseDTO.from(missionsService.publishMission(missionId));
    }

    @PatchMapping("/missions/{missionId}/withdraw")
    public MissionResponseDTO withdraw(@PathVariable long missionId) {
        return MissionResponseDTO.from(missionsService.withdrawMission(missionId));
    }

    // Solo bozze mai pubblicate e senza lezioni né esercizi, altrimenti 409
    @DeleteMapping("/missions/{missionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long missionId) {
        missionsService.deleteMission(missionId);
    }
}
