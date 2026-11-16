package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.gamification.Accessory;
import giuliacrepaldi.BStudent_Management.entities.gamification.AccessoryBenefit;
import giuliacrepaldi.BStudent_Management.entities.gamification.AccessoryChargeState;
import giuliacrepaldi.BStudent_Management.entities.gamification.EquippedAccessory;
import giuliacrepaldi.BStudent_Management.entities.gamification.HintUse;
import giuliacrepaldi.BStudent_Management.entities.gamification.StudentAccessory;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintChargesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintReviewLinkDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.AccessoryBenefitsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.AccessoryChargeStatesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.EquippedAccessoriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.HintUsesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.StudentAccessoriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IHintsService;
import giuliacrepaldi.BStudent_Management.utilities.GameRules;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.ChargeState;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class HintsService implements IHintsService {

    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    private final StudentAccessoriesRepository studentAccessoriesRepository;
    private final EquippedAccessoriesRepository equippedRepository;
    private final AccessoryBenefitsRepository benefitsRepository;
    private final AccessoryChargeStatesRepository chargeStatesRepository;
    private final HintUsesRepository hintUsesRepository;
    private final MissionAttemptsRepository missionAttemptsRepository;
    private final MissionExercisesRepository missionExercisesRepository;
    private final ResourcesRepository resourcesRepository;
    private final StudentsRepository studentsRepository;
    private final AccessoriesService accessoriesService;
    private final EntityManager entityManager;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public HintsService(StudentAccessoriesRepository studentAccessoriesRepository,
                        EquippedAccessoriesRepository equippedRepository, AccessoryBenefitsRepository benefitsRepository,
                        AccessoryChargeStatesRepository chargeStatesRepository, HintUsesRepository hintUsesRepository,
                        MissionAttemptsRepository missionAttemptsRepository,
                        MissionExercisesRepository missionExercisesRepository, ResourcesRepository resourcesRepository,
                        StudentsRepository studentsRepository, AccessoriesService accessoriesService,
                        EntityManager entityManager) {
        this.studentAccessoriesRepository = studentAccessoriesRepository;
        this.equippedRepository = equippedRepository;
        this.benefitsRepository = benefitsRepository;
        this.chargeStatesRepository = chargeStatesRepository;
        this.hintUsesRepository = hintUsesRepository;
        this.missionAttemptsRepository = missionAttemptsRepository;
        this.missionExercisesRepository = missionExercisesRepository;
        this.resourcesRepository = resourcesRepository;
        this.studentsRepository = studentsRepository;
        this.accessoriesService = accessoriesService;
        this.entityManager = entityManager;
    }

    // Non readOnly: la lettura applica la ricarica scaduta
    @Override
    @Transactional
    public HintChargesDTO getCharges(long studentId) {
        // Prima quello indossato, altrimenti il primo posseduto che dà un aiuto
        Accessory equipped = equippedHintAccessory(studentId);
        if (equipped != null) {
            return accessoriesService.charges(studentId, equipped, benefitOf(equipped), true);
        }
        for (StudentAccessory owned : studentAccessoriesRepository.findById_StudentId(studentId)) {
            AccessoryBenefit benefit = benefitsRepository.findById(owned.getId().getAccessoryId()).orElse(null);
            if (benefit != null) {
                return accessoriesService.charges(studentId, owned.getAccessory(), benefit, false);
            }
        }
        return HintChargesDTO.none();
    }

    @Override
    @Transactional
    public HintDTO useHint(long studentId, long missionAttemptId, long missionExerciseId) {
        MissionAttempt attempt = findAttempt(studentId, missionAttemptId);
        if (!AccessoriesService.OPEN_ATTEMPT.contains(attempt.getStatus())) {
            throw new ConflictException("La missione è già conclusa: l'aiuto si usa mentre la svolgi");
        }
        MissionExercise exercise = missionExercisesRepository.findById(missionExerciseId)
                .orElseThrow(() -> new NotFoundException("Esercizio " + missionExerciseId + " non trovato"));
        if (!exercise.getMission().getMissionId().equals(attempt.getMissionRun().getMission().getMissionId())) {
            throw new BadRequestException("L'esercizio non fa parte di questa missione");
        }
        Accessory accessory = equippedHintAccessory(studentId);
        if (accessory == null) {
            throw new BadRequestException("Indossa il cappello da turista per usare l'aiuto");
        }
        AccessoryBenefit benefit = benefitOf(accessory);
        Topic topic = exercise.getQuestionBankEntry().getTopic();
        if (topic == null) {
            // Nessuna carica consumata (§6.2)
            throw new BadRequestException("Per questo esercizio il cappello non ha suggerimenti");
        }
        // hint_id = esercizio della missione: riaprire lo stesso aiuto nello stesso tentativo è gratuito (§11.3)
        HintUse.HintUseId useId = new HintUse.HintUseId(studentId, missionAttemptId, missionExerciseId);
        boolean alreadyOpened = hintUsesRepository.existsById(useId);
        HintChargesDTO charges = accessoriesService.charges(studentId, accessory, benefit, true);
        if (!alreadyOpened) {
            if (charges.remaining() == 0) {
                throw new ConflictException("Hai finito gli aiuti: tornano alle "
                        + charges.rechargeAt().atZoneSameInstant(ROME).format(HOUR));
            }
            AccessoryChargeState state = chargeStatesRepository.findById(
                    new AccessoryChargeState.AccessoryChargeStateId(studentId, accessory.getAccessoryId()))
                    .orElseThrow(() -> new NotFoundException("Cariche dell'accessorio non trovate"));
            ChargeState after = GameRules.chargesAfterUse(
                    new ChargeState(state.getRemainingCharges(), state.getRechargeAt()), benefit.getRechargeHours(),
                    OffsetDateTime.now());
            state.setRemainingCharges(after.remaining());
            state.setRechargeAt(after.rechargeAt());
            // persist e non save: con la chiave composta già valorizzata save farebbe un merge
            entityManager.persist(HintUse.builder().id(useId)
                    .student(studentsRepository.getReferenceById(studentId))
                    .missionAttempt(attempt).accessory(accessory).build());
            entityManager.flush();
            charges = new HintChargesDTO(true, true, accessory.getAccessoryName(), after.remaining(),
                    benefit.getCapacity(), after.rechargeAt());
        }
        // L'"In breve" si vede anche se la Risorsa è premium: è un aiuto guadagnato (§6.2).
        // Argomento senza Risorsa pubblicata: il cappello dice solo l'argomento
        Resource resource = resourceOf(topic);
        return new HintDTO(topic.getTopicName(), resource == null ? null : resource.getSummaryText(),
                resource == null ? null : summaryExample(resource), alreadyOpened, charges);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HintReviewLinkDTO> getReviewLinks(long studentId, long missionAttemptId) {
        MissionAttempt attempt = findAttempt(studentId, missionAttemptId);
        // Il collegamento alla Risorsa intera compare solo a tentativo chiuso (§6.2)
        if (AccessoriesService.OPEN_ATTEMPT.contains(attempt.getStatus())) {
            return List.of();
        }
        List<HintReviewLinkDTO> links = new ArrayList<>();
        List<Long> seenTopics = new ArrayList<>();
        for (HintUse use : hintUsesRepository.findById_StudentIdAndId_MissionAttemptId(studentId, missionAttemptId)) {
            Topic topic = missionExercisesRepository.findById(use.getId().getHintId())
                    .map(exercise -> exercise.getQuestionBankEntry().getTopic()).orElse(null);
            if (topic == null || seenTopics.contains(topic.getTopicId())) {
                continue;
            }
            seenTopics.add(topic.getTopicId());
            Resource resource = resourceOf(topic);
            links.add(new HintReviewLinkDTO(topic.getTopicName(), topic.getSubject().getSlug(),
                    resource == null ? null : resource.getSlug(), resource == null ? null : resource.getTitle()));
        }
        return links;
    }

    private MissionAttempt findAttempt(long studentId, long missionAttemptId) {
        MissionAttempt attempt = missionAttemptsRepository.findById(missionAttemptId)
                .orElseThrow(() -> new NotFoundException("Tentativo di missione " + missionAttemptId + " non trovato"));
        // Un tentativo altrui è come se non esistesse
        if (!attempt.getStudent().getStudentId().equals(studentId)) {
            throw new NotFoundException("Tentativo di missione " + missionAttemptId + " non trovato");
        }
        return attempt;
    }

    private Accessory equippedHintAccessory(long studentId) {
        for (EquippedAccessory equipped : equippedRepository.findById_StudentId(studentId)) {
            if (benefitsRepository.existsById(equipped.getAccessory().getAccessoryId())) {
                return equipped.getAccessory();
            }
        }
        return null;
    }

    private AccessoryBenefit benefitOf(Accessory accessory) {
        return benefitsRepository.findById(accessory.getAccessoryId())
                .orElseThrow(() -> new NotFoundException("Aiuto dell'accessorio non trovato"));
    }

    private Resource resourceOf(Topic topic) {
        return resourcesRepository.findFirstByTopic_TopicIdAndStatusOrderByResourceId(topic.getTopicId(), ContentStatus.published)
                .orElse(null);
    }

    // Il mini esempio di "In breve" sta nel JSON del contenuto (content.summaryExample)
    private JsonNode summaryExample(Resource resource) {
        JsonNode example = jsonMapper.readTree(resource.getContent()).get("summaryExample");
        return example == null || example.isNull() ? null : example;
    }
}
