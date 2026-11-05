package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.catalog.SubjectDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.CoursesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ISubjectsService;
import giuliacrepaldi.BStudent_Management.utilities.Pageables;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class SubjectsService implements ISubjectsService {

    private static final Set<String> SORTS = Set.of("subjectId", "subjectName");

    private final SubjectsRepository subjectsRepository;
    private final CoursesRepository coursesRepository;
    private final SkillsRepository skillsRepository;

    public SubjectsService(SubjectsRepository subjectsRepository, CoursesRepository coursesRepository,
                           SkillsRepository skillsRepository) {
        this.subjectsRepository = subjectsRepository;
        this.coursesRepository = coursesRepository;
        this.skillsRepository = skillsRepository;
    }

    @Override
    @Transactional
    public Subject saveNewSubject(SubjectDTO body) {
        String name = body.name().strip();
        // Nessun vincolo nel DB: due materie con lo stesso nome confonderebbero catalogo e filtri
        if (subjectsRepository.existsBySubjectNameIgnoreCase(name)) {
            throw new ConflictException("Esiste già una materia chiamata " + name);
        }
        Subject subject = Subject.builder()
                .subjectName(name)
                .defaultSkill(findSkill(body.defaultSkillId()))
                .build();
        return subjectsRepository.save(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public Subject findById(long subjectId) {
        return subjectsRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException(subjectId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Subject> findAll(int page, int size, String sortBy) {
        return subjectsRepository.findAll(Pageables.of(page, size, sortBy, SORTS, "subjectName"));
    }

    @Override
    @Transactional
    public Subject findByIdAndUpdate(long subjectId, SubjectDTO body) {
        Subject subject = findById(subjectId);
        String name = body.name().strip();
        if (subjectsRepository.existsBySubjectNameIgnoreCaseAndSubjectIdNot(name, subjectId)) {
            throw new ConflictException("Esiste già una materia chiamata " + name);
        }
        subject.setSubjectName(name);
        // Vale per i pesi risolti da ora: i tentativi già svolti conservano i propri contributi
        subject.setDefaultSkill(findSkill(body.defaultSkillId()));
        return subject;
    }

    @Override
    @Transactional
    public void deleteSubject(long subjectId) {
        Subject subject = findById(subjectId);
        if (coursesRepository.existsBySubject_SubjectId(subjectId)) {
            throw new ConflictException("La materia ha dei corsi: eliminali o spostali prima");
        }
        // TODO: anche argomenti Palestra e test di orientamento puntano alla materia; oggi la FK
        // li blocca con un 409 generico, da rendere un messaggio esplicito quando si implementano
        subjectsRepository.delete(subject);
    }

    private Skill findSkill(Long skillId) {
        if (skillId == null) {
            return null;
        }
        return skillsRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill " + skillId + " non trovata"));
    }
}
