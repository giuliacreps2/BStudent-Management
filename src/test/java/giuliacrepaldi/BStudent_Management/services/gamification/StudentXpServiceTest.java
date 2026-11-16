package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.progression.StudentXpEntry;
import giuliacrepaldi.BStudent_Management.entities.skills.EvolutionStageRule;
import giuliacrepaldi.BStudent_Management.entities.skills.StudentEvolutionStage;
import giuliacrepaldi.BStudent_Management.enums.skills.EvolutionRuleKind;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionStageRuleDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.StudentXpEntriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.EvolutionStageRulesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentEvolutionStagesRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Regole del Blueprint Gamification §1 e §4: Base, Evoluzione I (primo corso), Evoluzione II (800 XP e media 3)
class StudentXpServiceTest {

    private static final long STUDENT = 7L;
    private static final long MISSION = 30L;

    private StudentXpEntriesRepository xpEntries;
    private StudentEvolutionStagesRepository stages;
    private EvolutionStageRulesRepository rules;
    private MissionExercisesRepository missionExercises;
    private MissionsRepository missions;
    private ISkillLevelsService skillLevels;
    private ILivesService lives;
    private EntityManager entityManager;
    private StudentXpService service;

    @BeforeEach
    void setUp() {
        xpEntries = mock(StudentXpEntriesRepository.class);
        stages = mock(StudentEvolutionStagesRepository.class);
        rules = mock(EvolutionStageRulesRepository.class);
        missionExercises = mock(MissionExercisesRepository.class);
        missions = mock(MissionsRepository.class);
        skillLevels = mock(ISkillLevelsService.class);
        lives = mock(ILivesService.class);
        entityManager = mock(EntityManager.class);
        when(rules.findAllByOrderByStageNumber()).thenReturn(List.of(
                new EvolutionStageRule(0, EvolutionRuleKind.start, null, null),
                new EvolutionStageRule(1, EvolutionRuleKind.first_course, null, null),
                new EvolutionStageRule(2, EvolutionRuleKind.thresholds, new BigDecimal("800"), new BigDecimal("3"))));
        service = new StudentXpService(xpEntries, stages, rules, missionExercises, missions, skillLevels, lives,
                entityManager);
    }

    private void student(int stage, String xp, String averageLevel) {
        when(stages.findCurrentStage(STUDENT)).thenReturn(stage);
        when(xpEntries.sumXp(STUDENT)).thenReturn(new BigDecimal(xp));
        when(skillLevels.getBestAverageLevel(STUDENT)).thenReturn(new BigDecimal(averageLevel));
    }

    private int reachedStage() {
        ArgumentCaptor<StudentEvolutionStage> captor = ArgumentCaptor.forClass(StudentEvolutionStage.class);
        verify(entityManager).persist(captor.capture());
        assertThat(captor.getValue().getId().getStudentId()).isEqualTo(STUDENT);
        return captor.getValue().getId().getStageNumber();
    }

    @Test
    void laPrimaVoltaLaMissioneDaGliXpDeiSuoiEsercizi() {
        student(0, "0", "1");
        when(missions.existsById(MISSION)).thenReturn(true);
        when(missionExercises.sumVisibleMaxPoints(MISSION)).thenReturn(new BigDecimal("50.00"));

        service.onMissionPassed(STUDENT, MISSION, 99L);

        ArgumentCaptor<StudentXpEntry> captor = ArgumentCaptor.forClass(StudentXpEntry.class);
        verify(xpEntries).save(captor.capture());
        assertThat(captor.getValue().getXp()).isEqualByComparingTo("50");
        assertThat(captor.getValue().getMissionId()).isEqualTo(MISSION);
    }

    @Test
    void ripetereLaMissioneNonRiaccreditaGliXp() {
        student(0, "50", "1");
        when(xpEntries.existsByStudentIdAndMissionId(STUDENT, MISSION)).thenReturn(true);

        service.onMissionPassed(STUDENT, MISSION, 100L);

        verify(xpEntries, never()).save(any(StudentXpEntry.class));
    }

    @Test
    void ilPrimoCorsoCompletatoDaLEvoluzioneIELeSetteVite() {
        student(0, "550", "2.50");

        assertThat(service.onCourseCompleted(STUDENT, 3L)).containsExactly(1);

        assertThat(reachedStage()).isEqualTo(1);
        verify(lives).raiseBaseLives(STUDENT, 7);
    }

    @Test
    void ilSecondoCorsoCompletatoNonAssegnaDiNuovoIlPremio() {
        student(1, "700", "2.50");

        assertThat(service.onCourseCompleted(STUDENT, 4L)).isEmpty();

        verify(entityManager, never()).persist(any());
        verify(lives, never()).raiseBaseLives(anyLong(), anyInt());
    }

    @Test
    void lEvoluzioneIIArrivaCon800XpEMedia3() {
        student(1, "800", "3.00");
        assertThat(service.evaluateEvolutions(STUDENT)).containsExactly(2);
        assertThat(reachedStage()).isEqualTo(2);
    }

    @Test
    void conLaMediaA2e8LEvoluzioneIINonArriva() {
        student(1, "2000", "2.80");
        assertThat(service.evaluateEvolutions(STUDENT)).isEmpty();
        verify(entityManager, never()).persist(any());
    }

    @Test
    void lEvoluzioneIIRichiedeLaI() {
        // Soglie raggiunte ma primo corso non completato: nessuno stadio
        student(0, "2000", "4.00");
        assertThat(service.evaluateEvolutions(STUDENT)).isEmpty();
        verify(entityManager, never()).persist(any());
    }

    @Test
    void ilProgressoDiceCheCosaMancaAllaProssimaEvoluzione() {
        student(1, "550", "2.80");
        EvolutionProgressDTO progress = service.getEvolutionProgress(STUDENT);
        assertThat(progress.currentStage()).isEqualTo(1);
        assertThat(progress.nextStage()).isEqualTo(2);
        assertThat(progress.xpMissing()).isEqualByComparingTo("250");
        assertThat(progress.averageLevelReached()).isFalse();

        // Allo stadio Base il prossimo passo è completare il primo corso
        student(0, "0", "1");
        assertThat(service.getEvolutionProgress(STUDENT).nextRequiresFirstCourse()).isTrue();

        // Dopo l'ultimo stadio non c'è un prossimo
        student(2, "900", "3.50");
        assertThat(service.getEvolutionProgress(STUDENT).nextStage()).isNull();
    }

    @Test
    void soloGliStadiConSoglieSiModificano() {
        when(rules.findById(1)).thenReturn(Optional.of(new EvolutionStageRule(1, EvolutionRuleKind.first_course, null, null)));
        assertThatThrownBy(() -> service.updateRule(1, new EvolutionStageRuleDTO(new BigDecimal("100"), new BigDecimal("2"))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("primo corso");
    }
}
