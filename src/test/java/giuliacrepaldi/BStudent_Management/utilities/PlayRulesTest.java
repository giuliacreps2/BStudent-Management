package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.utilities.PlayRules.Scored;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules.TimerState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Gli esempi sono quelli del blueprint principale (§6.2, §9, §10) e del Blueprint Svolgimento v0.1
class PlayRulesTest {

    private static final BigDecimal SIXTY = new BigDecimal("60.00");
    private static final OffsetDateTime START = OffsetDateTime.parse("2026-11-17T10:00:00+01:00");
    private static final long TOLERANCE = 45_000;

    private static BigDecimal n(String value) {
        return new BigDecimal(value);
    }

    // ------------------------------------------------------------------ Correzione automatica

    @Test
    void laSceltaMultiplaETuttoONiente() {
        assertThat(PlayRules.gradeChoice(4L, 4L)).isEqualByComparingTo("100");
        assertThat(PlayRules.gradeChoice(5L, 4L)).isEqualByComparingTo("0");
        assertThat(PlayRules.gradeChoice(null, 4L)).isEqualByComparingTo("0");
    }

    @Test
    void ilCompletamentoIgnoraMaiuscoleSpaziEsterniESpaziDoppi() {
        List<String> accepted = List.of("rosam", "Rosas");
        assertThat(PlayRules.gradeText("  ROSAM ", accepted)).isEqualByComparingTo("100");
        assertThat(PlayRules.gradeText("rosas", accepted)).isEqualByComparingTo("100");
        assertThat(PlayRules.gradeText("rosa", accepted)).isEqualByComparingTo("0");
        assertThat(PlayRules.gradeText("   ", accepted)).isEqualByComparingTo("0");
        assertThat(PlayRules.gradeText(null, accepted)).isEqualByComparingTo("0");
        // Uno spazio dentro la parola la rende un'altra risposta
        assertThat(PlayRules.gradeText("ros am", accepted)).isEqualByComparingTo("0");
        // Gli spazi doppi si ignorano; punteggiatura e accenti no
        assertThat(PlayRules.gradeText("rosam   et  lilium", List.of("rosam et lilium"))).isEqualByComparingTo("100");
        assertThat(PlayRules.gradeText("rosam, et lilium", List.of("rosam et lilium"))).isEqualByComparingTo("0");
        assertThat(PlayRules.gradeText("perche", List.of("perché"))).isEqualByComparingTo("0");
    }

    @Test
    void laStessaLetteraAccentataScrittaInDueModiELaStessaRisposta() {
        // "è" come un solo carattere oppure come "e" seguita dall'accento
        assertThat(PlayRules.gradeText("è bello", List.of("è bello"))).isEqualByComparingTo("100");
    }

    @Test
    void siSuperaConAlmenoLaSoglia() {
        assertThat(PlayRules.isPassed(n("60.00"), SIXTY)).isTrue();
        assertThat(PlayRules.isPassed(n("59.99"), SIXTY)).isFalse();
    }

    // ------------------------------------------------------------------ Tempo

    @Test
    void ilTempoScorreDaQuandoLEsercizioCompare() {
        // 20 minuti, aperto da 14: ne restano 6
        TimerState state = PlayRules.timer(20 * 60_000, START, START.plusMinutes(14), 15 * 60_000);
        assertThat(state.remainingMs()).isEqualTo(6 * 60_000);
        assertThat(state.expired()).isFalse();
    }

    @Test
    void ilTempoFermoNonScorre() {
        // Missione salvata o consegnata: nessun segno, il tempo resta quello
        TimerState state = PlayRules.timer(6 * 60_000, null, START.plusDays(3), TOLERANCE);
        assertThat(state).isEqualTo(new TimerState(6 * 60_000, false, false));
    }

    @Test
    void aTempoFinitoLEsercizioEScaduto() {
        TimerState state = PlayRules.timer(30_000, START, START.plusSeconds(30), TOLERANCE);
        assertThat(state.remainingMs()).isZero();
        assertThat(state.expired()).isTrue();
    }

    @Test
    void chiudereIlBrowserSalvaLaMissioneSenzaToglierePiuTempoDellaTolleranza() {
        // Ultima sincronizzazione con 6 minuti rimasti; lo studente torna due ore dopo
        TimerState state = PlayRules.timer(6 * 60_000, START, START.plusHours(2), TOLERANCE);
        assertThat(state.disconnected()).isTrue();
        assertThat(state.expired()).isFalse();
        assertThat(state.remainingMs()).isEqualTo(6 * 60_000 - TOLERANCE);
    }

    // ------------------------------------------------------------------ Punti

    @Test
    void inPalestraSottoIlSessantaNonCiSonoPunti() {
        assertThat(PlayRules.practiceContribution(n("10"), n("87"), SIXTY)).isEqualByComparingTo("8.70");
        assertThat(PlayRules.practiceContribution(n("10"), n("59.99"), SIXTY)).isEqualByComparingTo("0");
    }

    @Test
    void unNuovoRecordAccreditaSoloLaDifferenza() {
        // Massimo 10: 87% -> +8,70; 89% -> +0,20; 80% -> nulla; 100% -> +1,10 (§10.1)
        assertThat(PlayRules.improvement(n("0"), n("8.70"))).isEqualByComparingTo("8.70");
        assertThat(PlayRules.improvement(n("8.70"), n("8.90"))).isEqualByComparingTo("0.20");
        assertThat(PlayRules.improvement(n("8.90"), n("8.00"))).isEqualByComparingTo("0");
        assertThat(PlayRules.improvement(n("8.90"), n("10.00"))).isEqualByComparingTo("1.10");
    }

    @Test
    void iPuntiSkillDellaMissioneSeguonoLeQuoteEIlFattore() {
        // 10 punti, 70% Traduzione e 30% Lessico, risultato 80%, fattore 10%
        assertThat(PlayRules.missionSkillContribution(n("10"), n("70"), n("80"), n("0.1000"))).isEqualByComparingTo("0.56");
        assertThat(PlayRules.missionSkillContribution(n("10"), n("30"), n("80"), n("0.1000"))).isEqualByComparingTo("0.24");
        // Tutto su una skill: 70% -> 0,70; rifatto con 90% -> +0,20 (Svolgimento §5)
        BigDecimal first = PlayRules.missionSkillContribution(n("10"), n("100"), n("70"), n("0.1000"));
        BigDecimal second = PlayRules.missionSkillContribution(n("10"), n("100"), n("90"), n("0.1000"));
        assertThat(first).isEqualByComparingTo("0.70");
        assertThat(PlayRules.improvement(first, second)).isEqualByComparingTo("0.20");
        // I punti skill maturano anche sotto la soglia (§10.3)
        assertThat(PlayRules.missionSkillContribution(n("10"), n("100"), n("40"), n("0.1000"))).isEqualByComparingTo("0.40");
    }

    @Test
    void ilRisultatoDellaMissionePesaGliEserciziPerIPunti() {
        // 10 punti al 100% e 20 punti al 70% -> 80%
        assertThat(PlayRules.missionPercentage(List.of(new Scored(n("10"), n("100")), new Scored(n("20"), n("70")))))
                .isEqualByComparingTo("80");
        assertThat(PlayRules.missionPercentage(List.of())).isEqualByComparingTo("100");
    }

    @Test
    void laMediaDelCorsoDaLoStessoPesoAOgniMissione() {
        assertThat(PlayRules.courseAverage(List.of(n("80"), n("90"), n("100")))).isEqualByComparingTo("90");
        assertThat(PlayRules.courseAverage(List.of())).isNull();
    }
}
