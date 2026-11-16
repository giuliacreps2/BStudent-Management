package giuliacrepaldi.BStudent_Management.services.practice;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.practice.PracticeExerciseDTO;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationRecommendationsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.services.exercises.interfaces.IQuestionBankService;
import giuliacrepaldi.BStudent_Management.services.practice.interfaces.IPracticeCatalogService;
import giuliacrepaldi.BStudent_Management.services.practice.interfaces.ITopicsService;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISubjectSkillsService;
import giuliacrepaldi.BStudent_Management.utilities.Pageables;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class PracticeCatalogService implements IPracticeCatalogService {

    private static final Set<String> SORTS =
            Set.of("practiceExerciseId", "questionBankEntry.title", "status", "maxPoints", "publishedAt");

    private final PracticeExercisesRepository practiceExercisesRepository;
    private final OrientationRecommendationsRepository recommendationsRepository;
    private final SkillsRepository skillsRepository;
    private final IQuestionBankService questionBankService;
    private final ITopicsService topicsService;
    private final ISubjectSkillsService subjectSkillsService;

    public PracticeCatalogService(PracticeExercisesRepository practiceExercisesRepository,
                                  OrientationRecommendationsRepository recommendationsRepository,
                                  SkillsRepository skillsRepository, IQuestionBankService questionBankService,
                                  ITopicsService topicsService, ISubjectSkillsService subjectSkillsService) {
        this.practiceExercisesRepository = practiceExercisesRepository;
        this.recommendationsRepository = recommendationsRepository;
        this.skillsRepository = skillsRepository;
        this.questionBankService = questionBankService;
        this.topicsService = topicsService;
        this.subjectSkillsService = subjectSkillsService;
    }

    @Override
    @Transactional
    public PracticeExercise saveNewPracticeExercise(PracticeExerciseDTO body) {
        QuestionBankEntry entry = questionBankService.findById(body.questionBankEntryId());
        if (entry.getStatus() == ContentStatus.withdrawn) {
            throw new ConflictException("L'esercizio è ritirato nel banco: non si aggiunge alla Palestra");
        }
        // UNIQUE nel DB: lo stesso esercizio compare una sola volta in Palestra
        if (practiceExercisesRepository.existsByQuestionBankEntry_QuestionBankEntryId(entry.getQuestionBankEntryId())) {
            throw new ConflictException("L'esercizio è già nella Palestra");
        }
        PracticeExercise exercise = PracticeExercise.builder()
                .questionBankEntry(entry)
                .status(ContentStatus.draft)
                .build();
        applyContent(exercise, body);
        return practiceExercisesRepository.save(exercise);
    }

    @Override
    @Transactional(readOnly = true)
    public PracticeExercise findById(long practiceExerciseId) {
        return practiceExercisesRepository.findById(practiceExerciseId)
                .orElseThrow(() -> new NotFoundException(practiceExerciseId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PracticeExercise> findAll(Long subjectId, Long topicId, Long skillId, ContentStatus status,
                                          int page, int size, String sortBy) {
        return practiceExercisesRepository.search(subjectId, topicId, skillId, status,
                Pageables.of(page, size, sortBy, SORTS, "practiceExerciseId"));
    }

    @Override
    @Transactional
    public PracticeExercise findByIdAndUpdate(long practiceExerciseId, PracticeExerciseDTO body) {
        PracticeExercise exercise = findById(practiceExerciseId);
        // Il body lo ripete, ma la voce resta legata al suo esercizio (tentativi e record lo usano)
        if (!exercise.getQuestionBankEntry().getQuestionBankEntryId().equals(body.questionBankEntryId())) {
            throw new BadRequestException("L'esercizio collegato a una voce della Palestra non si cambia");
        }
        // Ammessa in ogni stato; i punti massimi nuovi valgono da ora, i record già ottenuti restano (§10.1)
        applyContent(exercise, body);
        return exercise;
    }

    @Override
    @Transactional
    public PracticeExercise publishPracticeExercise(long practiceExerciseId) {
        PracticeExercise exercise = findById(practiceExerciseId);
        if (exercise.getStatus() == ContentStatus.published) {
            throw new ConflictException("La voce è già pubblicata in Palestra");
        }
        // Esercizio pubblicato = ha una versione pubblicata e il titolo per lo studente
        if (exercise.getQuestionBankEntry().getStatus() != ContentStatus.published) {
            throw new ConflictException("Pubblica prima l'esercizio nel banco");
        }
        exercise.setStatus(ContentStatus.published);
        // published_at è la prima pubblicazione: resta anche dopo un ritiro e un ripristino
        if (exercise.getPublishedAt() == null) {
            exercise.setPublishedAt(OffsetDateTime.now());
        }
        exercise.setWithdrawnAt(null);
        return exercise;
    }

    @Override
    @Transactional
    public PracticeExercise withdrawPracticeExercise(long practiceExerciseId) {
        PracticeExercise exercise = findById(practiceExerciseId);
        if (exercise.getStatus() != ContentStatus.published) {
            throw new ConflictException("Si può ritirare solo una voce pubblicata");
        }
        // Nasconde la voce; tentativi e record restano per lo storico.
        // TODO (Blocco 5): bozze salvate e consegne in attesa su una voce ritirata
        exercise.setStatus(ContentStatus.withdrawn);
        exercise.setWithdrawnAt(OffsetDateTime.now());
        return exercise;
    }

    @Override
    @Transactional
    public void deletePracticeExercise(long practiceExerciseId) {
        PracticeExercise exercise = findById(practiceExerciseId);
        if (exercise.getStatus() != ContentStatus.draft || exercise.getPublishedAt() != null) {
            throw new ConflictException("Una voce già pubblicata non si elimina: va ritirata");
        }
        if (recommendationsRepository.existsById_PracticeExerciseId(practiceExerciseId)) {
            throw new ConflictException("La voce è consigliata da un test: toglila prima dalle risposte del test");
        }
        practiceExercisesRepository.delete(exercise);
    }

    private void applyContent(PracticeExercise exercise, PracticeExerciseDTO body) {
        Topic topic = topicsService.findById(body.topicId());
        Skill skill = skillsRepository.findById(body.skillId())
                .orElseThrow(() -> new NotFoundException("Skill " + body.skillId() + " non trovata"));
        // La materia della voce si ricava dall'argomento (§7.1): la skill deve essere di quella materia
        subjectSkillsService.assertSkillOfSubject(topic.getSubject().getSubjectId(), skill.getSkillId());
        exercise.setTopic(topic);
        exercise.setSkill(skill);
        exercise.setMaxPoints(body.maxPoints());
        exercise.setEstimatedMinutes(body.estimatedMinutes() == null ? null : body.estimatedMinutes().shortValue());
        exercise.setAccessTier(body.accessTier());
    }
}
