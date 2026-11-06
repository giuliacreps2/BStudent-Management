package giuliacrepaldi.BStudent_Management.services.skills.interfaces;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.payloads.skills.SkillDTO;
import org.springframework.data.domain.Page;

/**
 * Catalogo skill e livelli (blueprint, appendice).
 * Livello e soglia punti unici nella skill.
 * <p>
 * Il code è l'identificativo stabile (come per i ruoli): fissato alla creazione, non si modifica.
 * TODO stati della skill: StatusSkill ha solo draft; decidere se servono published/withdrawn
 * prima di mostrare le skill allo studente (Blocco 4, "I miei progressi").
 * TODO livelli della skill (skill_levels) quando si definiscono le soglie (blueprint §10.4).
 */
public interface ISkillsService {

    //Admin
    //CRUD -> create, read, update, delete

    Skill saveNewSkill(SkillDTO body); // 409 se il code esiste già

    Skill findById(long skillId);

    Page<Skill> findAll(int page, int size, String sortBy);

    Skill findByIdAndUpdate(long skillId, SkillDTO body); // nome e descrizione; il code non cambia

    void deleteSkill(long skillId); // solo se nessuna tabella la usa, altrimenti ConflictException
}
