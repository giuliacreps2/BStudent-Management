package giuliacrepaldi.BStudent_Management.services.exercises;

import giuliacrepaldi.BStudent_Management.entities.catalog.AcademySetting;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.exercises.MissionExerciseDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.SkillWeightDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.AcademySettingsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionStepsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExerciseSkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.IMissionsService;
import giuliacrepaldi.BStudent_Management.services.exercises.interfaces.IMissionExercisesService;
import giuliacrepaldi.BStudent_Management.services.exercises.interfaces.IQuestionBankService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MissionExercisesService implements IMissionExercisesService {

    private static final short ACADEMY_SETTINGS_ID = 1;
    private static final BigDecimal FULL_WEIGHT = BigDecimal.valueOf(100);

    private final MissionExercisesRepository missionExercisesRepository;
    private final MissionExerciseSkillsRepository skillWeightsRepository;
    private final ExerciseAttemptsRepository exerciseAttemptsRepository;
    private final AcademySettingsRepository academySettingsRepository;
    private final SkillsRepository skillsRepository;
    private final MissionStepsRepository missionStepsRepository;
    private final IMissionsService missionsService;
    private final IQuestionBankService questionBankService;
    private final EntityManager entityManager;

    public MissionExercisesService(MissionExercisesRepository missionExercisesRepository,
                                   MissionExerciseSkillsRepository skillWeightsRepository,
                                   ExerciseAttemptsRepository exerciseAttemptsRepository,
                                   AcademySettingsRepository academySettingsRepository,
                                   SkillsRepository skillsRepository,
                                   MissionStepsRepository missionStepsRepository,
                                   IMissionsService missionsService,
                                   IQuestionBankService questionBankService, EntityManager entityManager) {
        this.missionExercisesRepository = missionExercisesRepository;
        this.skillWeightsRepository = skillWeightsRepository;
        this.exerciseAttemptsRepository = exerciseAttemptsRepository;
        this.academySettingsRepository = academySettingsRepository;
        this.skillsRepository = skillsRepository;
        this.missionStepsRepository = missionStepsRepository;
        this.missionsService = missionsService;
        this.questionBankService = questionBankService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public MissionExercise assignExercise(long missionId, MissionExerciseDTO body) {
        if (body.questionBankEntryId() == null) {
            throw new BadRequestException("questionBankEntryId è obbligatorio");
        }
        Mission mission = missionsService.findById(missionId);
        QuestionBankEntry entry = questionBankService.findById(body.questionBankEntryId());
        // Una bozza si può collegare (la pubblicazione la richiede pubblicata), un ritirato no
        if (entry.getStatus() == ContentStatus.withdrawn) {
            throw new ConflictException("L'esercizio è ritirato");
        }
        if (missionExercisesRepository.existsByMission_MissionIdAndQuestionBankEntry_QuestionBankEntryId(
                missionId, entry.getQuestionBankEntryId())) {
            throw new ConflictException("L'esercizio è già usato in questa missione");
        }

        // Valori sempre espliciti: i default di oggi restano salvati anche se academy_settings cambia
        AcademySetting settings = academySettings();
        // Nasce draft: in una missione pubblicata resta nascosto finché non lo si pubblica
        MissionExercise exercise = missionExercisesRepository.save(MissionExercise.builder()
                .mission(mission)
                .questionBankEntry(entry)
                .status(ContentStatus.draft)
                .allowedDurationMs(body.allowedDurationMs() != null
                        ? body.allowedDurationMs() : settings.getDefaultAllowedDurationMs())
                .maxPoints(body.maxPoints() != null ? body.maxPoints() : settings.getDefaultMaxPoints())
                .build());
        // In coda alla sequenza; nascosto, quindi la sequenza vista dallo studente non cambia
        missionStepsRepository.save(MissionStep.of(exercise, missionStepsRepository.findMaxSortOrder(missionId) + 1));
        if (body.skills() != null) {
            replaceSkillWeights(exercise.getMissionExerciseId(), body.skills());
        }
        return exercise;
    }

    @Override
    @Transactional
    public MissionExercise publishExercise(long missionExerciseId) {
        MissionExercise exercise = findById(missionExerciseId);
        if (exercise.getStatus() == ContentStatus.published) {
            throw new ConflictException("L'esercizio è già pubblicato in questa missione");
        }
        // Esercizio pubblicato con una versione pubblicata, e skill indicate o skill predefinita della materia
        List<String> problems = missionsService.exerciseProblems(exercise);
        if (!problems.isEmpty()) {
            throw new ConflictException("L'esercizio non è pronto: " + String.join("; ", problems));
        }
        exercise.setStatus(ContentStatus.published);
        // In una missione pubblicata compare subito: non può seguire un altro esercizio né aprire la missione
        missionsService.assertSequenceStillValid(exercise.getMission());
        return exercise;
    }

    @Override
    @Transactional(readOnly = true)
    public MissionExercise findById(long missionExerciseId) {
        return missionExercisesRepository.findById(missionExerciseId)
                .orElseThrow(() -> new NotFoundException(missionExerciseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionExercise> findByMission(long missionId) {
        missionsService.findById(missionId);
        return missionExercisesRepository.findByMissionOrdered(missionId);
    }

    @Override
    @Transactional
    public MissionExercise findByIdAndUpdate(long missionExerciseId, MissionExerciseDTO body) {
        MissionExercise exercise = findById(missionExerciseId);
        // I tentativi sono legati alla coppia (collegamento, esercizio): cambiarlo riscriverebbe lo storico
        if (body.questionBankEntryId() != null
                && !Objects.equals(body.questionBankEntryId(), exercise.getQuestionBankEntry().getQuestionBankEntryId())) {
            throw new BadRequestException("Per cambiare esercizio scollega quello attuale e collega il nuovo");
        }
        // Ammesso anche dopo i primi tentativi: ognuno conserva il proprio tempo (allowed_duration_ms)
        if (body.allowedDurationMs() != null) {
            exercise.setAllowedDurationMs(body.allowedDurationMs());
        }
        if (body.maxPoints() != null) {
            exercise.setMaxPoints(body.maxPoints());
        }
        return exercise;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MissionExerciseSkill> findSkillWeights(long missionExerciseId) {
        findById(missionExerciseId);
        return skillWeightsRepository.findWithSkill(missionExerciseId);
    }

    @Override
    @Transactional
    public List<MissionExerciseSkill> replaceSkillWeights(long missionExerciseId, List<SkillWeightDTO> weights) {
        MissionExercise exercise = findById(missionExerciseId);

        Set<Long> skillIds = new HashSet<>();
        for (SkillWeightDTO weight : weights) {
            if (!skillIds.add(weight.skillId())) {
                throw new BadRequestException("La skill " + weight.skillId() + " compare più volte");
            }
        }
        if (!weights.isEmpty()) {
            BigDecimal sum = weights.stream().map(SkillWeightDTO::weight).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(FULL_WEIGHT) != 0) {
                throw new BadRequestException("La somma dei pesi deve essere 100 (ora è " + sum.stripTrailingZeros().toPlainString() + ")");
            }
        }
        Map<Long, Skill> skills = skillsRepository.findAllById(skillIds).stream()
                .collect(Collectors.toMap(Skill::getSkillId, Function.identity()));
        if (skills.size() != skillIds.size()) {
            Set<Long> missing = new HashSet<>(skillIds);
            missing.removeAll(skills.keySet());
            throw new NotFoundException("Skill non trovate: " + missing);
        }

        // Sostituzione in blocco: DELETE eseguito subito, poi i nuovi INSERT.
        // Vale per i contributi calcolati da ora: quelli già maturati restano (blueprint §10.3)
        skillWeightsRepository.deleteByMissionExerciseId(missionExerciseId);
        for (SkillWeightDTO weight : weights) {
            // persist e non save: con la chiave composta già valorizzata save farebbe un merge
            entityManager.persist(MissionExerciseSkill.builder()
                    .id(new MissionExerciseSkill.MissionExerciseSkillId(missionExerciseId, weight.skillId()))
                    .missionExercise(exercise)
                    .skill(skills.get(weight.skillId()))
                    .weight(weight.weight())
                    .build());
        }
        return skillWeightsRepository.findWithSkill(missionExerciseId);
    }

    @Override
    @Transactional
    public void removeExercise(long missionExerciseId) {
        MissionExercise exercise = findById(missionExerciseId);
        if (exerciseAttemptsRepository.existsByMissionExercise_MissionExerciseId(missionExerciseId)) {
            throw new ConflictException("L'esercizio è già stato svolto in questa missione: non si scollega");
        }
        // In una missione pubblicata deve restare almeno un esercizio (due lezioni di seguito sono ammesse)
        missionStepsRepository.deleteByMissionExerciseId(missionExerciseId);
        missionsService.assertSequenceStillValid(exercise.getMission());
        skillWeightsRepository.deleteByMissionExerciseId(missionExerciseId);
        missionExercisesRepository.delete(exercise);
    }

    // Riga singola creata dalla migrazione v0.6: se manca, il DB non è allineato
    private AcademySetting academySettings() {
        return academySettingsRepository.findById(ACADEMY_SETTINGS_ID)
                .orElseThrow(() -> new IllegalStateException("academy_settings non inizializzata: applicare la migrazione v0.6"));
    }
}
