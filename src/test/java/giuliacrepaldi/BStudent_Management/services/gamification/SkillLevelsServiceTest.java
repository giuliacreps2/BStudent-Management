package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.entities.skills.SkillLevel;
import giuliacrepaldi.BStudent_Management.entities.skills.StudentStartingLevel;
import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeLockDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelsDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.SkillCreditEntriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillLevelsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentStartingLevelsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SubjectSkillsRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Regole del Blueprint Gamification §2, §3 e §4.2. Latino = materia 1, Greco = materia 2
class SkillLevelsServiceTest {

    private static final long STUDENT = 7L;
    private static final long LATIN = 1L;
    private static final long GREEK = 2L;
    private static final long GRAMMAR = 10L;
    private static final long LEXICON = 11L;

    private SkillLevelsRepository levels;
    private SkillCreditEntriesRepository credits;
    private SubjectSkillsRepository subjectSkills;
    private StudentStartingLevelsRepository startingLevels;
    private PracticeExercisesRepository practiceExercises;
    private SkillsRepository skills;
    private SubjectsRepository subjects;
    private SkillLevelsService service;

    private final Skill grammar = skill(GRAMMAR, "Grammatica");
    private final Skill lexicon = skill(LEXICON, "Lessico");

    @BeforeEach
    void setUp() {
        levels = mock(SkillLevelsRepository.class);
        credits = mock(SkillCreditEntriesRepository.class);
        subjectSkills = mock(SubjectSkillsRepository.class);
        startingLevels = mock(StudentStartingLevelsRepository.class);
        practiceExercises = mock(PracticeExercisesRepository.class);
        skills = mock(SkillsRepository.class);
        subjects = mock(SubjectsRepository.class);
        service = new SkillLevelsService(levels, credits, subjectSkills, startingLevels, practiceExercises, skills,
                subjects, mock(EntityManager.class));

        // Curva della beta per entrambe le skill
        List<SkillLevel> all = new ArrayList<>(curve(grammar, "0", "20", "50", "95", "165", "265"));
        all.addAll(curve(lexicon, "0", "20", "50", "95", "165", "265"));
        when(levels.findBySkillIds(anyCollection())).thenReturn(all);
        when(levels.findBySkill_SkillIdOrderByLevelNumber(GRAMMAR))
                .thenReturn(curve(grammar, "0", "20", "50", "95", "165", "265"));
        when(skills.findById(GRAMMAR)).thenReturn(Optional.of(grammar));
        when(subjects.existsById(anyLong())).thenReturn(true);
        when(startingLevels.findById_StudentId(STUDENT)).thenReturn(List.of());
    }

    private static Skill skill(long id, String name) {
        return Skill.builder().skillId(id).codeSkill(name.toLowerCase()).skillName(name).build();
    }

    private static List<SkillLevel> curve(Skill skill, String... minPoints) {
        List<SkillLevel> result = new ArrayList<>();
        for (int i = 0; i < minPoints.length; i++) {
            result.add(SkillLevel.builder().skill(skill).levelNumber(i + 1).minPoints(new BigDecimal(minPoints[i])).build());
        }
        return result;
    }

    // Righe della vista dei totali: materia, skill, punti
    private void points(Object[]... rows) {
        when(credits.findTotalsByStudent(STUDENT)).thenReturn(List.of(rows));
    }

    private static Object[] row(long subjectId, long skillId, String points) {
        return new Object[]{subjectId, skillId, new BigDecimal(points)};
    }

    private PracticeExercise practice(long id, long subjectId, Skill skill, int difficulty) {
        Subject subject = new Subject();
        subject.setSubjectId(subjectId);
        return PracticeExercise.builder().practiceExerciseId(id).skill(skill).difficulty(difficulty)
                .topic(Topic.builder().subject(subject).build()).build();
    }

    @Test
    void iLivelliSiContanoPerMateria() {
        // 60 punti di Grammatica in Latino, nessuno in Greco
        points(row(LATIN, GRAMMAR, "60.00"));
        assertThat(service.getLevel(STUDENT, LATIN, GRAMMAR)).isEqualTo(3);
        assertThat(service.getLevel(STUDENT, GREEK, GRAMMAR)).isEqualTo(1);
    }

    @Test
    void laSchedaDellaSkillDiceQuantoMancaAlProssimoLivello() {
        points(row(LATIN, GRAMMAR, "60.00"));
        when(subjectSkills.findBySubjectOrdered(LATIN)).thenReturn(List.of(
                SubjectSkill.builder().id(new SubjectSkill.SubjectSkillId(LATIN, GRAMMAR)).skill(grammar).sortOrder(1).build(),
                SubjectSkill.builder().id(new SubjectSkill.SubjectSkillId(LATIN, LEXICON)).skill(lexicon).sortOrder(2).build()));

        List<SkillLevelStateDTO> result = service.getLevels(STUDENT, LATIN);

        assertThat(result).extracting(SkillLevelStateDTO::name).containsExactly("Grammatica", "Lessico");
        assertThat(result.get(0).level()).isEqualTo(3);
        assertThat(result.get(0).pointsToNextLevel()).isEqualByComparingTo("35");
        assertThat(result.get(1).level()).isEqualTo(1);
        assertThat(result.get(1).pointsToNextLevel()).isEqualByComparingTo("20");
    }

    @Test
    void ilLivelloDiPartenzaAlzaIlLivelloMaNonIPuntiVeri() {
        points(row(LATIN, GRAMMAR, "10.00"));
        when(startingLevels.findById_StudentId(STUDENT)).thenReturn(List.of(StudentStartingLevel.builder()
                .id(new StudentStartingLevel.StudentStartingLevelId(STUDENT, LATIN, GRAMMAR)).levelNumber(3).build()));

        assertThat(service.getLevel(STUDENT, LATIN, GRAMMAR)).isEqualTo(3);
        // Per il cappello contano solo i punti guadagnati
        assertThat(service.getBestPoints(STUDENT, GRAMMAR)).isEqualByComparingTo("10");
    }

    @Test
    void perGliAccessoriValeLaMateriaMigliore() {
        points(row(LATIN, GRAMMAR, "30.00"), row(GREEK, GRAMMAR, "55.00"), row(GREEK, LEXICON, "99.00"));
        assertThat(service.getBestPoints(STUDENT, GRAMMAR)).isEqualByComparingTo("55");
    }

    @Test
    void laMediaUsaLaMateriaMiglioreESoloLeSkillConEsercizi() {
        // Latino: Grammatica 4 (95 punti) e Lessico 2 (20 punti) -> media 3. Greco appena iniziato -> media 1
        points(row(LATIN, GRAMMAR, "95.00"), row(LATIN, LEXICON, "20.00"), row(GREEK, GRAMMAR, "5.00"));
        when(subjectSkills.findSkillIdsWithPublishedExercises(LATIN)).thenReturn(List.of(GRAMMAR, LEXICON));
        when(subjectSkills.findSkillIdsWithPublishedExercises(GREEK)).thenReturn(List.of(GRAMMAR, LEXICON));

        assertThat(service.getAverageLevel(STUDENT, LATIN)).isEqualByComparingTo("3");
        assertThat(service.getAverageLevel(STUDENT, GREEK)).isEqualByComparingTo("1");
        assertThat(service.getBestAverageLevel(STUDENT)).isEqualByComparingTo("3");

        // Se Lessico non ha ancora esercizi pubblicati in Latino, non abbassa la media
        when(subjectSkills.findSkillIdsWithPublishedExercises(LATIN)).thenReturn(List.of(GRAMMAR));
        assertThat(service.getAverageLevel(STUDENT, LATIN)).isEqualByComparingTo("4");
    }

    @Test
    void unaVoceEBloccataOltreIlLivelloPiuUno() {
        // Grammatica livello 2 in Latino: aperte le difficoltà 1-3
        points(row(LATIN, GRAMMAR, "20.00"));
        when(practiceExercises.findAllById(any())).thenReturn(List.of(
                practice(100, LATIN, grammar, 3), practice(101, LATIN, grammar, 4), practice(102, GREEK, grammar, 3)));

        var locks = service.getLocks(STUDENT, List.of(100L, 101L, 102L));

        assertThat(locks.get(100L).locked()).isFalse();
        assertThat(locks.get(101L)).isEqualTo(new PracticeLockDTO(101L, 4, true, 3, "Si sblocca al livello 3 di Grammatica"));
        // In Greco la stessa skill è al livello 1: la difficoltà 3 è chiusa
        assertThat(locks.get(102L).locked()).isTrue();
        assertThat(locks.get(102L).message()).isEqualTo("Si sblocca al livello 2 di Grammatica");
    }

    @Test
    void leSoglieSiPossonoAbbassareEIlLivelliAggiungereMaNonAlzareNeTogliere() {
        // Aggiungere il livello 7 e abbassare una soglia: ammesso
        service.replaceLevels(GRAMMAR, new SkillLevelsDTO(numbers("0", "20", "45", "95", "165", "265", "400"), false));
        verify(levels).deleteBySkillId(GRAMMAR);
        verify(levels, times(7)).save(any(SkillLevel.class));

        // Alzare una soglia farebbe scendere chi l'aveva superata
        assertThatThrownBy(() -> service.replaceLevels(GRAMMAR,
                new SkillLevelsDTO(numbers("0", "30", "50", "95", "165", "265"), false)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("non alzarle");

        // Togliere un livello pure
        assertThatThrownBy(() -> service.replaceLevels(GRAMMAR,
                new SkillLevelsDTO(numbers("0", "20", "50", "95", "165"), false)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("non si possono togliere");
    }

    @Test
    void laCurvaParteDaZeroECresce() {
        assertThatThrownBy(() -> service.replaceLevels(GRAMMAR, new SkillLevelsDTO(numbers("5", "20"), false)))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.replaceLevels(GRAMMAR, new SkillLevelsDTO(numbers("0", "20", "20"), false)))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("livello 3");
        verify(levels, never()).deleteBySkillId(anyLong());
    }

    private static List<BigDecimal> numbers(String... values) {
        return Stream.of(values).map(BigDecimal::new).toList();
    }
}
