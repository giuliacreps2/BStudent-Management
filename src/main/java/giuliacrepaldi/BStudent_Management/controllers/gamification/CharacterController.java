package giuliacrepaldi.BStudent_Management.controllers.gamification;

import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterAdminDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterAdminResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterStageDTO;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISkinsService;
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

// Solo ADMIN: SecurityConfig protegge /admin/academy/**. Personaggi e immagini dei loro stadi
@RestController
@RequestMapping("/admin/academy/characters")
public class CharacterController {

    private final ISkinsService skinsService;

    public CharacterController(ISkinsService skinsService) {
        this.skinsService = skinsService;
    }

    @GetMapping
    public List<CharacterAdminResponseDTO> findAll() {
        return skinsService.findAll();
    }

    @GetMapping("/{characterId}")
    public CharacterAdminResponseDTO findById(@PathVariable long characterId) {
        return skinsService.findById(characterId);
    }

    // Body: { "code": "marco", "name": "Marco", "description": "...", "motto": "...", "starter": true }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CharacterAdminResponseDTO create(@RequestBody @Valid CharacterAdminDTO body) {
        return skinsService.save(body);
    }

    // Il code nel body deve restare quello attuale
    @PutMapping("/{characterId}")
    public CharacterAdminResponseDTO update(@PathVariable long characterId, @RequestBody @Valid CharacterAdminDTO body) {
        return skinsService.update(characterId, body);
    }

    // Crea o aggiorna lo stadio (0 = Base, 1 = Evoluzione I...).
    // Body: { "name": "Evoluzione I", "description": "...", "imageReference": "bstudent/marco-1" }
    @PutMapping("/{characterId}/stages/{stageNumber}")
    public CharacterAdminResponseDTO saveStage(@PathVariable long characterId, @PathVariable int stageNumber,
                                               @RequestBody @Valid CharacterStageDTO body) {
        return skinsService.saveStage(characterId, stageNumber, body);
    }

    // 409 se manca l'immagine di uno stadio
    @PostMapping("/{characterId}/publish")
    public CharacterAdminResponseDTO publish(@PathVariable long characterId) {
        return skinsService.publish(characterId);
    }

    // 409 se uno studente lo ha già scelto
    @PostMapping("/{characterId}/withdraw")
    public CharacterAdminResponseDTO withdraw(@PathVariable long characterId) {
        return skinsService.withdraw(characterId);
    }

    @DeleteMapping("/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long characterId) {
        skinsService.delete(characterId);
    }
}
