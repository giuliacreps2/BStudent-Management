package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.catalog.SubjectDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.CoursesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SubjectSkillsRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ISubjectsService;
import giuliacrepaldi.BStudent_Management.utilities.Pageables;
import giuliacrepaldi.BStudent_Management.utilities.Slugs;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class SubjectsService implements ISubjectsService {

    private static final Set<String> SORTS = Set.of("subjectId", "subjectName");

    private final SubjectsRepository subjectsRepository;
    private final CoursesRepository coursesRepository;
    private final SkillsRepository skillsRepository;
    private final SubjectSkillsRepository subjectSkillsRepository;

    public SubjectsService(SubjectsRepository subjectsRepository, CoursesRepository coursesRepository,
                           SkillsRepository skillsRepository, SubjectSkillsRepository subjectSkillsRepository) {
        this.subjectsRepository = subjectsRepository;
        this.coursesRepository = coursesRepository;
        this.skillsRepository = skillsRepository;
        this.subjectSkillsRepository = subjectSkillsRepository;
    }

    @Override
    @Transactional
    public Subject saveNewSubject(SubjectDTO body) {
        String name = body.name().strip();
        // Nessun vincolo nel DB: due materie con lo stesso nome confonderebbero catalogo e filtri
        if (subjectsRepository.existsBySubjectNameIgnoreCase(name)) {
            throw new ConflictException("Esiste già una materia chiamata " + name);
        }
        String slug = Slugs.resolve(body.slug(), name);
        if (subjectsRepository.existsBySlug(slug)) {
            throw new ConflictException("Esiste già una materia con l'indirizzo " + slug);
        }
        Subject subject = Subject.builder()
                .subjectName(name)
                .slug(slug)
                .status(SubjectStatus.draft)
                .defaultSkill(findSkill(body.defaultSkillId()))
                .build();
        applyContent(subject, body);
        Subject saved = subjectsRepository.save(subject);
        addDefaultToSubjectSkills(saved);
        return saved;
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
        String slug = Slugs.resolve(body.slug(), name);
        if (subjectsRepository.existsBySlugAndSubjectIdNot(slug, subjectId)) {
            throw new ConflictException("Esiste già una materia con l'indirizzo " + slug);
        }
        subject.setSubjectName(name);
        subject.setSlug(slug);
        applyContent(subject, body);
        // Vale per i pesi risolti da ora: i tentativi già svolti conservano i propri contributi
        subject.setDefaultSkill(findSkill(body.defaultSkillId()));
        addDefaultToSubjectSkills(subject);
        return subject;
    }

    @Override
    @Transactional
    public Subject publishSubject(long subjectId) {
        Subject subject = findById(subjectId);
        if (subject.getStatus() == SubjectStatus.published) {
            throw new ConflictException("La materia è già pubblicata");
        }
        // Nessun requisito sui corsi: senza corsi pubblicati appare come "Presto disponibile"
        subject.setStatus(SubjectStatus.published);
        if (subject.getPublishedAt() == null) {
            subject.setPublishedAt(OffsetDateTime.now());
        }
        return subject;
    }

    @Override
    @Transactional
    public Subject unpublishSubject(long subjectId) {
        Subject subject = findById(subjectId);
        if (subject.getStatus() == SubjectStatus.draft) {
            throw new ConflictException("La materia è già nascosta");
        }
        // I contenuti restano pubblicati ma non si vedono: lo studente vede un elemento
        // solo se anche i suoi contenitori sono visibili
        subject.setStatus(SubjectStatus.draft);
        return subject;
    }

    private static void applyContent(Subject subject, SubjectDTO body) {
        subject.setTagline(blankToNull(body.tagline()));
        subject.setImageReference(blankToNull(body.imageReference()));
        subject.setGuideName(blankToNull(body.guideName()));
        subject.setGuideImageReference(blankToNull(body.guideImageReference()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
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

    // La skill predefinita è sempre tra le skill della materia: se manca entra in coda (blueprint §10.4-ter)
    private void addDefaultToSubjectSkills(Subject subject) {
        Skill skill = subject.getDefaultSkill();
        if (skill == null
                || subjectSkillsRepository.existsById_SubjectIdAndId_SkillId(subject.getSubjectId(), skill.getSkillId())) {
            return;
        }
        subjectSkillsRepository.save(SubjectSkill.builder()
                .id(new SubjectSkill.SubjectSkillId(subject.getSubjectId(), skill.getSkillId()))
                .subject(subject)
                .skill(skill)
                .sortOrder(subjectSkillsRepository.findMaxSortOrder(subject.getSubjectId()) + 1)
                .build());
    }

    private Skill findSkill(Long skillId) {
        if (skillId == null) {
            return null;
        }
        return skillsRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill " + skillId + " non trovata"));
    }
}
