package giuliacrepaldi.BStudent_Management.controllers.catalog;

import giuliacrepaldi.BStudent_Management.payloads.catalog.MissionDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.MissionResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.ReorderDTO;
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

    //STATO: cambi di stato senza body; 409 se la transizione non è ammessa o la missione non è pronta
    @PatchMapping("/missions/{missionId}/publish")
    public MissionResponseDTO publish(@PathVariable long missionId) {
        return MissionResponseDTO.from(missionsService.publishMission(missionId));
    }

    @PatchMapping("/missions/{missionId}/withdraw")
    public MissionResponseDTO withdraw(@PathVariable long missionId) {
        return MissionResponseDTO.from(missionsService.withdrawMission(missionId));
    }

    // Solo bozze mai pubblicate e senza lezioni, altrimenti 409
    @DeleteMapping("/missions/{missionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long missionId) {
        missionsService.deleteMission(missionId);
    }
}
