package giuliacrepaldi.BStudent_Management.services.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.SkillCreditEntry;
import giuliacrepaldi.BStudent_Management.enums.progression.SourceKind;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.SkillCreditEntriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.ISkillCreditsService;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SkillCreditsService implements ISkillCreditsService {

    private final SkillCreditEntriesRepository creditEntriesRepository;
    private final StudentsRepository studentsRepository;
    private final SkillsRepository skillsRepository;
    private final SubjectsRepository subjectsRepository;

    public SkillCreditsService(SkillCreditEntriesRepository creditEntriesRepository, StudentsRepository studentsRepository,
                               SkillsRepository skillsRepository, SubjectsRepository subjectsRepository) {
        this.creditEntriesRepository = creditEntriesRepository;
        this.studentsRepository = studentsRepository;
        this.skillsRepository = skillsRepository;
        this.subjectsRepository = subjectsRepository;
    }

    @Override
    @Transactional
    public BigDecimal creditImprovement(long studentId, long subjectId, long skillId, SourceKind sourceKind, long sourceId,
                                        BigDecimal contribution, String eventId) {
        // Stesso evento e stessa skill: già accreditato (il vincolo unico del DB resta l'ultima difesa)
        if (creditEntriesRepository.existsByEventIdAndSkill_SkillId(eventId, skillId)) {
            return PlayRules.ZERO;
        }
        BigDecimal best = creditEntriesRepository.findBestContribution(studentId, skillId, sourceKind, sourceId);
        BigDecimal delta = PlayRules.improvement(best, contribution);
        if (delta.signum() == 0) {
            return PlayRules.ZERO;
        }
        creditEntriesRepository.save(SkillCreditEntry.builder()
                .eventId(eventId)
                .delta(delta)
                .previousContribution(best)
                .newContribution(best.add(delta))
                .sourceKind(sourceKind)
                .sourceId(sourceId)
                .student(studentsRepository.getReferenceById(studentId))
                .skill(skillsRepository.getReferenceById(skillId))
                .subject(subjectsRepository.getReferenceById(subjectId))
                .build());
        return delta;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBestContribution(long studentId, long skillId, SourceKind sourceKind, long sourceId) {
        return creditEntriesRepository.findBestContribution(studentId, skillId, sourceKind, sourceId);
    }
}
