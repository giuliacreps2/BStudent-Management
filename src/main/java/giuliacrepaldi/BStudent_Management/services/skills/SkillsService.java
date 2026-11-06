package giuliacrepaldi.BStudent_Management.services.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.skills.SkillDTO;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISkillsService;
import giuliacrepaldi.BStudent_Management.utilities.Pageables;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class SkillsService implements ISkillsService {

    private static final Set<String> SORTS = Set.of("skillId", "codeSkill", "skillName");

    private final SkillsRepository skillsRepository;

    public SkillsService(SkillsRepository skillsRepository) {
        this.skillsRepository = skillsRepository;
    }

    @Override
    @Transactional
    public Skill saveNewSkill(SkillDTO body) {
        // Il vincolo unique del DB resta l'ultima difesa (409 generico) in caso di creazioni contemporanee
        if (skillsRepository.existsByCodeSkill(body.code())) {
            throw new ConflictException("Esiste già una skill con code " + body.code());
        }
        Skill skill = Skill.builder()
                .codeSkill(body.code())
                .skillName(body.name().strip())
                .skillDescription(blankToNull(body.description()))
                .build();
        return skillsRepository.save(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public Skill findById(long skillId) {
        return skillsRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException(skillId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Skill> findAll(int page, int size, String sortBy) {
        return skillsRepository.findAll(Pageables.of(page, size, sortBy, SORTS, "skillName"));
    }

    @Override
    @Transactional
    public Skill findByIdAndUpdate(long skillId, SkillDTO body) {
        Skill skill = findById(skillId);
        // Il code è l'identificativo stabile: il body lo ripete, ma non può cambiarlo
        if (!skill.getCodeSkill().equals(body.code())) {
            throw new BadRequestException("Il code di una skill non si modifica");
        }
        skill.setSkillName(body.name().strip());
        skill.setSkillDescription(blankToNull(body.description()));
        return skill;
    }

    @Override
    @Transactional
    public void deleteSkill(long skillId) {
        Skill skill = findById(skillId);
        if (skillsRepository.isInUse(skillId)) {
            throw new ConflictException("La skill è usata (pesi, materie, Palestra, accessori o progressi): non si elimina");
        }
        skillsRepository.delete(skill);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
