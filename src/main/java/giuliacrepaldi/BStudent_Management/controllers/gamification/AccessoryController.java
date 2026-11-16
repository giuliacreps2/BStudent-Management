package giuliacrepaldi.BStudent_Management.controllers.gamification;

import giuliacrepaldi.BStudent_Management.payloads.gamification.AccessoryDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.AccessoryResponseDTO;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IAccessoriesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**. Accessori, soglie e aiuto
@RestController
@RequestMapping("/admin/academy/accessories")
public class AccessoryController {

    private final IAccessoriesService accessoriesService;

    public AccessoryController(IAccessoriesService accessoriesService) {
        this.accessoriesService = accessoriesService;
    }

    @GetMapping
    public List<AccessoryResponseDTO> findAll() {
        return accessoriesService.findAll();
    }

    @GetMapping("/{accessoryId}")
    public AccessoryResponseDTO findById(@PathVariable long accessoryId) {
        return accessoriesService.findById(accessoryId);
    }

    // Body: { "code": "cappello-da-turista", "name": "Cappello da turista", "slotCode": "testa", "skillId": 7,
    //         "requiredPoints": 50, "available": true, "hintCapacity": 5, "hintRechargeHours": 12 }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccessoryResponseDTO create(@RequestBody @Valid AccessoryDTO body) {
        return accessoriesService.save(body);
    }

    // code e slotCode nel body devono restare quelli attuali
    @PutMapping("/{accessoryId}")
    public AccessoryResponseDTO update(@PathVariable long accessoryId, @RequestBody @Valid AccessoryDTO body) {
        return accessoriesService.update(accessoryId, body);
    }

    // 409 se uno studente lo possiede
    @DeleteMapping("/{accessoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long accessoryId) {
        accessoriesService.delete(accessoryId);
    }
}
