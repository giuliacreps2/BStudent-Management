package giuliacrepaldi.BStudent_Management.services.skills;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SubjectSkillsRepository;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISubjectSkillsService;
import giuliacrepaldi.BStudent_Management.utilities.Reorders;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SubjectSkillsService implements ISubjectSkillsService {

    private static final String SORT_ORDER_CONSTRAINT = "subject_skills_subject_id_sort_order_idx";

    private final SubjectSkillsRepository subjectSkillsRepository;
    private final SubjectsRepository subjectsRepository;
    private final SkillsRepository skillsRepository;
    private final EntityManager entityManager;

    // I repository e non ISubjectsService: anche SubjectsService aggiunge la skill predefinita qui
    public SubjectSkillsService(SubjectSkillsRepository subjectSkillsRepository, SubjectsRepository subjectsRepository,
                                SkillsRepository skillsRepository, EntityManager entityManager) {
        this.subjectSkillsRepository = subjectSkillsRepository;
        this.subjectsRepository = subjectsRepository;
        this.skillsRepository = skillsRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectSkill> findBySubject(long subjectId) {
        // 404 se la materia non esiste, invece di una lista vuota
        findSubject(subjectId);
        return subjectSkillsRepository.findBySubjectOrdered(subjectId);
    }

    @Override
    @Transactional
    public List<SubjectSkill> addSkill(long subjectId, long skillId) {
        Subject subject = findSubject(subjectId);
        Skill skill = skillsRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill " + skillId + " non trovata"));
        if (subjectSkillsRepository.existsById_SubjectIdAndId_SkillId(subjectId, skillId)) {
            throw new ConflictException("La materia usa già la skill " + skill.getSkillName());
        }
        // In coda: due aggiunte contemporanee sulla stessa materia violano l'unicità -> 409
        // persist e non save: con la chiave composta già valorizzata save farebbe un merge
        entityManager.persist(SubjectSkill.builder()
                .id(new SubjectSkill.SubjectSkillId(subjectId, skillId))
                .subject(subject)
                .skill(skill)
                .sortOrder(subjectSkillsRepository.findMaxSortOrder(subjectId) + 1)
                .build());
        entityManager.flush();
        return subjectSkillsRepository.findBySubjectOrdered(subjectId);
    }

    @Override
    @Transactional
    public List<SubjectSkill> removeSkill(long subjectId, long skillId) {
        Subject subject = findSubject(subjectId);
        SubjectSkill subjectSkill = subjectSkillsRepository.findById(new SubjectSkill.SubjectSkillId(subjectId, skillId))
                .orElseThrow(() -> new NotFoundException("La materia non usa la skill " + skillId));
        String skillName = subjectSkill.getSkill().getSkillName();
        if (subject.getDefaultSkill() != null && subject.getDefaultSkill().getSkillId() == skillId) {
            throw new ConflictException(skillName + " è la skill predefinita della materia: scegline un'altra prima di toglierla");
        }
        List<String> usages = new ArrayList<>();
        long practice = subjectSkillsRepository.countPracticeExercisesUsing(subjectId, skillId);
        if (practice > 0) {
            usages.add(practice + " esercizi della Palestra");
        }
        long weights = subjectSkillsRepository.countMissionWeightsUsing(subjectId, skillId);
        if (weights > 0) {
            usages.add(weights + " esercizi delle missioni");
        }
        if (!usages.isEmpty()) {
            throw new ConflictException(skillName + " è usata da " + String.join(" e ", usages) + ": cambia prima la loro skill");
        }
        // Il buco nell'ordine (es. 1, 3) non viola il vincolo: si sistema con il riordino
        subjectSkillsRepository.delete(subjectSkill);
        entityManager.flush();
        return subjectSkillsRepository.findBySubjectOrdered(subjectId);
    }

    @Override
    @Transactional
    public List<SubjectSkill> reorderSkills(long subjectId, List<Long> skillIds) {
        findSubject(subjectId);
        List<SubjectSkill> current = subjectSkillsRepository.findBySubjectOrdered(subjectId);
        Map<Long, SubjectSkill> bySkillId = current.stream()
                .collect(Collectors.toMap(s -> s.getId().getSkillId(), Function.identity()));
        Reorders.validateIds(skillIds, bySkillId.keySet(), "skill della materia");
        Reorders.deferUniqueOrder(entityManager, SORT_ORDER_CONSTRAINT);
        for (int i = 0; i < skillIds.size(); i++) {
            bySkillId.get(skillIds.get(i)).setSortOrder(i + 1);
        }
        entityManager.flush();
        return subjectSkillsRepository.findBySubjectOrdered(subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public void assertSkillOfSubject(long subjectId, long skillId) {
        if (!subjectSkillsRepository.existsById_SubjectIdAndId_SkillId(subjectId, skillId)) {
            String name = skillsRepository.findById(skillId).map(Skill::getSkillName).orElse("con id " + skillId);
            throw new BadRequestException("La skill " + name + " non è tra le skill della materia: aggiungila prima nella materia");
        }
    }

    private Subject findSubject(long subjectId) {
        return subjectsRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException(subjectId));
    }
}
