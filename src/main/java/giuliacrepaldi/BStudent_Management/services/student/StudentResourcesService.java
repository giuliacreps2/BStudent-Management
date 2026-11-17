package giuliacrepaldi.BStudent_Management.services.student;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.notebook.ResourceBookmark;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.student.ResourceCardDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.ResourceDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.ResourcesHubDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.SubjectCardDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.CoursesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.notebook.ResourceBookmarksRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentResourcesService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Service
public class StudentResourcesService implements IStudentResourcesService {

    private static final int SEARCH_LIMIT = 20;

    private final SubjectsRepository subjectsRepository;
    private final CoursesRepository coursesRepository;
    private final ResourcesRepository resourcesRepository;
    private final ResourceBookmarksRepository bookmarksRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;
    private final IEntitlementService entitlementService;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public StudentResourcesService(SubjectsRepository subjectsRepository, CoursesRepository coursesRepository,
                                   ResourcesRepository resourcesRepository, ResourceBookmarksRepository bookmarksRepository,
                                   PracticeExercisesRepository practiceExercisesRepository,
                                   IEntitlementService entitlementService) {
        this.subjectsRepository = subjectsRepository;
        this.coursesRepository = coursesRepository;
        this.resourcesRepository = resourcesRepository;
        this.bookmarksRepository = bookmarksRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
        this.entitlementService = entitlementService;
    }

    @Override
    @Transactional(readOnly = true)
    public ResourcesHubDTO getHub(long studentId, String subjectSlug) {
        List<Subject> subjects = subjectsRepository.findByStatusOrderBySubjectId(SubjectStatus.published);
        List<SubjectCardDTO> cards = subjects.stream().map(this::card).toList();
        Subject active = subjects.stream().filter(s -> s.getSlug().equals(subjectSlug)).findFirst()
                .orElse(subjects.isEmpty() ? null : subjects.get(0));
        if (active == null) {
            return new ResourcesHubDTO(cards, null, List.of(), List.of(), List.of());
        }
        boolean premium = entitlementService.hasPremium(studentId);
        List<ResourceCardDTO> all = resourcesRepository
                .findBySubject_SubjectIdAndStatusOrderByTitle(active.getSubjectId(), ContentStatus.published).stream()
                .map(resource -> ResourceCardDTO.from(resource, isLocked(resource, premium))).toList();
        // "Più usate" e "in evidenza" le sceglie l'autrice finché non ci sono i dati d'uso (§7.4)
        return new ResourcesHubDTO(cards, card(active), all.stream().filter(ResourceCardDTO::mostUsed).toList(),
                all.stream().filter(ResourceCardDTO::featured).toList(), all);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceDetailDTO getResource(long studentId, String subjectSlug, String resourceSlug) {
        Subject subject = findSubject(subjectSlug);
        Resource resource = resourcesRepository.findBySubject_SubjectIdAndSlug(subject.getSubjectId(), resourceSlug)
                .filter(StudentVisibility::isVisible)
                .orElseThrow(() -> new NotFoundException("Risorsa non trovata"));
        boolean locked = isLocked(resource, entitlementService.hasPremium(studentId));
        boolean hasExercises = resource.getTopic() != null
                && !practiceExercisesRepository.findVisibleByTopic(resource.getTopic().getTopicId()).isEmpty();
        return new ResourceDetailDTO(resource.getResourceId(), ResourceCardDTO.from(resource, locked), resource.getSummaryText(),
                // Con il lucchetto premium restano titolo, descrizione e "In breve": il contenuto non si invia
                locked ? null : jsonMapper.readTree(resource.getContent()),
                resource.getTopic() == null ? null : resource.getTopic().getTopicId(),
                resource.getTopic() == null ? null : resource.getTopic().getTopicName(), hasExercises,
                bookmarksRepository.existsById(new ResourceBookmark.ResourceBookmarkId(studentId, resource.getResourceId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceCardDTO> search(long studentId, String subjectSlug, String query) {
        Subject subject = findSubject(subjectSlug);
        String text = query == null ? "" : query.strip();
        if (text.length() < 2) {
            return List.of();
        }
        boolean premium = entitlementService.hasPremium(studentId);
        // I caratteri jolly scritti dallo studente valgono come testo
        String escaped = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return resourcesRepository.searchVisible(subject.getSubjectId(), escaped, PageRequest.of(0, SEARCH_LIMIT)).stream()
                .map(resource -> ResourceCardDTO.from(resource, isLocked(resource, premium))).toList();
    }

    private static boolean isLocked(Resource resource, boolean premium) {
        return resource.getAccessTier() == AccessTier.premium && !premium;
    }

    private SubjectCardDTO card(Subject subject) {
        return SubjectCardDTO.from(subject,
                coursesRepository.existsBySubject_SubjectIdAndStatus(subject.getSubjectId(), ContentStatus.published));
    }

    private Subject findSubject(String subjectSlug) {
        return subjectsRepository.findBySlug(subjectSlug)
                .filter(StudentVisibility::isVisible)
                .orElseThrow(() -> new NotFoundException("Materia non trovata"));
    }
}
