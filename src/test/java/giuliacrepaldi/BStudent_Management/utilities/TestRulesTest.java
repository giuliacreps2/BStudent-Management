package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.utilities.TestRules.Recommendation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel.advanced;
import static giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel.beginner;
import static giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel.intermediate;
import static org.assertj.core.api.Assertions.assertThat;

// Regole dei test dello studente (blueprint §8; Blueprint Gamification §2.6)
class TestRulesTest {

    @Test
    void ilLivelloEIlPiuBassoSceltoAlmenoDueVolte() {
        assertThat(TestRules.levelFor(List.of(advanced, intermediate, intermediate, advanced))).isEqualTo(intermediate);
        assertThat(TestRules.levelFor(List.of(advanced, advanced, beginner))).isEqualTo(advanced);
        assertThat(TestRules.levelFor(List.of(beginner, beginner, advanced, advanced))).isEqualTo(beginner);
    }

    @Test
    void seNessunLivelloSiRipeteValeIlPiuBasso() {
        assertThat(TestRules.levelFor(List.of(advanced, intermediate, beginner))).isEqualTo(beginner);
        assertThat(TestRules.levelFor(List.of(advanced, intermediate))).isEqualTo(intermediate);
    }

    @Test
    void ilTestDiLivelloDaUnaPartenzaPrudente() {
        assertThat(TestRules.startingLevelFor(beginner)).isEqualTo(1);
        assertThat(TestRules.startingLevelFor(intermediate)).isEqualTo(2);
        assertThat(TestRules.startingLevelFor(advanced)).isEqualTo(3);
    }

    @Test
    void primaLeVociConsigliateDaPiuRispostePoiLaPrioritaPoiLId() {
        List<List<Recommendation>> byAnswer = List.of(
                List.of(new Recommendation(10, 2), new Recommendation(11, 1)),
                List.of(new Recommendation(10, 3), new Recommendation(12, 1)),
                List.of(new Recommendation(13, 1)));
        // La 10 è consigliata da due risposte; poi 11, 12 e 13 a pari priorità, in ordine di ID
        assertThat(TestRules.recommend(byAnswer, 5)).containsExactly(10L, 11L, 12L, 13L);
        assertThat(TestRules.recommend(byAnswer, 2)).containsExactly(10L, 11L);
        assertThat(TestRules.recommend(List.of(), 5)).isEmpty();
    }
}
