package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.enums.gamification.AccessoryStatus;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.ChargeState;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.LevelState;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.PracticeOutcome;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.PracticeRun;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.Streak;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

// Gli esempi sono quelli del Blueprint Gamification v0.1
class GameRulesTest {

    private static final List<BigDecimal> CURVE = Stream.of("0", "20", "50", "95", "165", "265")
            .map(BigDecimal::new).toList();

    private static BigDecimal n(String value) {
        return new BigDecimal(value);
    }

    // ------------------------------------------------------------------ Livelli

    @Test
    void ilLivelloSegueLeSoglieDellaCurva() {
        assertThat(GameRules.levelFor(n("0"), CURVE)).isEqualTo(1);
        assertThat(GameRules.levelFor(n("19.99"), CURVE)).isEqualTo(1);
        assertThat(GameRules.levelFor(n("20"), CURVE)).isEqualTo(2);
        assertThat(GameRules.levelFor(n("60"), CURVE)).isEqualTo(3);
        assertThat(GameRules.levelFor(n("265"), CURVE)).isEqualTo(6);
        assertThat(GameRules.levelFor(n("9999"), CURVE)).isEqualTo(6);
    }

    @Test
    void con60PuntiMancano35AlLivello4() {
        LevelState state = GameRules.levelState(n("60"), CURVE);
        assertThat(state.level()).isEqualTo(3);
        assertThat(state.nextMinPoints().subtract(n("60"))).isEqualByComparingTo("35");
        // 10 punti sui 45 del livello: 22%
        assertThat(state.progressPercent()).isEqualTo(22);
        assertThat(state.maxLevel()).isFalse();
    }

    @Test
    void laBarraDelLivello2ArrivaAMetaCon35Punti() {
        assertThat(GameRules.levelState(n("35"), CURVE).progressPercent()).isEqualTo(50);
    }

    @Test
    void alLivelloMassimoLaBarraEPienaENonCEUnProssimo() {
        LevelState state = GameRules.levelState(n("300"), CURVE);
        assertThat(state.level()).isEqualTo(6);
        assertThat(state.maxLevel()).isTrue();
        assertThat(state.nextMinPoints()).isNull();
        assertThat(state.progressPercent()).isEqualTo(100);
    }

    @Test
    void ilLivelloDiPartenzaValeComePuntiSoloPerIlLivello() {
        // Partenza al livello 3 (soglia 50): barra a zero, dopo 45 punti veri si sale al 4
        assertThat(GameRules.levelPoints(n("0"), 3, CURVE)).isEqualByComparingTo("50");
        assertThat(GameRules.levelState(GameRules.levelPoints(n("0"), 3, CURVE), CURVE).progressPercent()).isZero();
        assertThat(GameRules.levelFor(GameRules.levelPoints(n("44.99"), 3, CURVE), CURVE)).isEqualTo(3);
        assertThat(GameRules.levelFor(GameRules.levelPoints(n("45"), 3, CURVE), CURVE)).isEqualTo(4);
        // Senza test di orientamento, o con livello 1, contano solo i punti veri
        assertThat(GameRules.levelPoints(n("12"), null, CURVE)).isEqualByComparingTo("12");
        assertThat(GameRules.levelPoints(n("12"), 1, CURVE)).isEqualByComparingTo("12");
        // Oltre il massimo: portato al massimo
        assertThat(GameRules.levelPoints(n("0"), 9, CURVE)).isEqualByComparingTo("265");
    }

    @Test
    void laVoceEApertaFinoAlLivelloPiuUno() {
        assertThat(GameRules.isUnlocked(2, 1)).isTrue();
        assertThat(GameRules.isUnlocked(3, 1)).isFalse();
        assertThat(GameRules.isUnlocked(6, 5)).isTrue();
        assertThat(GameRules.isUnlocked(6, 6)).isTrue();
        // Difficoltà 4: "Si sblocca al livello 3"
        assertThat(GameRules.unlockLevel(4)).isEqualTo(3);
    }

    @Test
    void laMediaDeiLivelliNonSiArrotondaInSu() {
        // Grammatica 4, Lessico 3, Analisi 2, Traduzione 3, Metrica 2 -> 2,8
        assertThat(GameRules.averageLevel(List.of(4, 3, 2, 3, 2))).isEqualByComparingTo("2.8");
        assertThat(GameRules.averageLevel(List.of(4, 3, 3, 3, 2))).isEqualByComparingTo("3");
        assertThat(GameRules.averageLevel(List.of(3, 3, 2))).isEqualByComparingTo("2.66");
        assertThat(GameRules.averageLevel(List.of())).isEqualByComparingTo("1");
    }

    // ------------------------------------------------------------------ Evoluzioni

    @Test
    void lEvoluzioneIIRichiedeXpEMediaInsieme() {
        assertThat(GameRules.meetsStage(n("800"), n("3.00"), n("800"), n("3"))).isTrue();
        assertThat(GameRules.meetsStage(n("799.99"), n("4.00"), n("800"), n("3"))).isFalse();
        assertThat(GameRules.meetsStage(n("2000"), n("2.80"), n("800"), n("3"))).isFalse();
    }

    // ------------------------------------------------------------------ Vite

    @Test
    void laRicaricaPortaIlSaldoAlmenoAllaBase() {
        assertThat(GameRules.livesAfterRecharge(3, 5)).isEqualTo(5);
        assertThat(GameRules.livesAfterRecharge(7, 5)).isEqualTo(7);
        assertThat(GameRules.livesAfterRecharge(3, 7)).isEqualTo(7);
        assertThat(GameRules.livesAfterRecharge(9, 7)).isEqualTo(9);
    }

    @Test
    void laPrimaEvoluzionePortaIlSaldoAlmenoASette() {
        assertThat(GameRules.livesAfterBaseRaise(3, 7)).isEqualTo(7);
        assertThat(GameRules.livesAfterBaseRaise(9, 7)).isEqualTo(9);
    }

    // ------------------------------------------------------------------ Cappello

    @Test
    void leCaricheScendonoELUltimaAvviaLaRicaricaDi12Ore() {
        OffsetDateTime now = OffsetDateTime.parse("2026-11-16T10:00:00+01:00");
        ChargeState afterFirst = GameRules.chargesAfterUse(new ChargeState(5, null), 12, now);
        assertThat(afterFirst).isEqualTo(new ChargeState(4, null));

        ChargeState afterLast = GameRules.chargesAfterUse(new ChargeState(1, null), 12, now);
        assertThat(afterLast.remaining()).isZero();
        assertThat(afterLast.rechargeAt()).isEqualTo(now.plusHours(12));

        // Prima della scadenza nulla cambia; alla scadenza le cariche tornano complete
        assertThat(GameRules.chargesAfterRecharge(afterLast, 5, now.plusHours(11))).isEqualTo(afterLast);
        assertThat(GameRules.chargesAfterRecharge(afterLast, 5, now.plusHours(12))).isEqualTo(new ChargeState(5, null));
    }

    // ------------------------------------------------------------------ Accessori

    @Test
    void ilCappelloSiRiscattaA50PuntiENonSiIndossaDaSolo() {
        assertThat(GameRules.accessoryStatus(n("49.99"), n("50"), false, false)).isEqualTo(AccessoryStatus.locked);
        assertThat(GameRules.accessoryStatus(n("50"), n("50"), false, false)).isEqualTo(AccessoryStatus.redeemable);
        assertThat(GameRules.accessoryStatus(n("50"), n("50"), true, false)).isEqualTo(AccessoryStatus.owned);
        assertThat(GameRules.accessoryStatus(n("50"), n("50"), true, true)).isEqualTo(AccessoryStatus.equipped);
        // Posseduto resta posseduto anche se la soglia viene alzata
        assertThat(GameRules.accessoryStatus(n("50"), n("100"), true, false)).isEqualTo(AccessoryStatus.owned);
    }

    // ------------------------------------------------------------------ Serie

    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 16);

    private static List<LocalDate> days(int... offsetsFromMonday) {
        List<LocalDate> result = new ArrayList<>();
        for (int offset : offsetsFromMonday) {
            result.add(MONDAY.plusDays(offset));
        }
        return result;
    }

    @Test
    void unGiornoDiPausaNonAzzeraLaSerie() {
        // lun, mar, (mer pausa), gio, ven -> 4
        assertThat(GameRules.streakOf(days(0, 1, 3, 4), MONDAY.plusDays(4))).isEqualTo(new Streak(4, 4));
    }

    @Test
    void dueGiorniDiPausaDiFilaLaInterromponoMaIlRecordResta() {
        // lun, mar, (mer, gio pausa), ven -> 1, record 2
        assertThat(GameRules.streakOf(days(0, 1, 4), MONDAY.plusDays(4))).isEqualTo(new Streak(1, 2));
    }

    @Test
    void oggiAncoraSenzaStudioNonInterrompe() {
        // lun e mar studiati: mercoledì e giovedì la Serie è ancora 2, venerdì è interrotta
        assertThat(GameRules.streakOf(days(0, 1), MONDAY.plusDays(2))).isEqualTo(new Streak(2, 2));
        assertThat(GameRules.streakOf(days(0, 1), MONDAY.plusDays(3))).isEqualTo(new Streak(2, 2));
        assertThat(GameRules.streakOf(days(0, 1), MONDAY.plusDays(4))).isEqualTo(new Streak(0, 2));
    }

    @Test
    void senzaStudioLaSerieEZero() {
        assertThat(GameRules.streakOf(List.of(), MONDAY)).isEqualTo(new Streak(0, 0));
    }

    // ------------------------------------------------------------------ Tris, sfida, pausa

    private static PracticeOutcome passed(long id) {
        return new PracticeOutcome(id, MONDAY, true, true);
    }

    private static PracticeOutcome failed(long id) {
        return new PracticeOutcome(id, MONDAY, false, true);
    }

    @Test
    void treSuperatiDiFilaDannoUnaVitaEUnErroreAzzera() {
        // superato, superato, non superato, superato, superato, superato -> 1 vita al sesto
        List<PracticeOutcome> outcomes = List.of(passed(1), passed(2), failed(3), passed(4), passed(5), passed(6));
        assertThat(GameRules.practiceRun(outcomes.subList(0, 5), 3)).isEqualTo(new PracticeRun(2, 0, 0));
        assertThat(GameRules.practiceRun(outcomes, 3)).isEqualTo(new PracticeRun(0, 1, 0));
    }

    @Test
    void laStessaVoceContaUnaVoltaSolaAlGiorno() {
        // Ripetere lo stesso esercizio non porta al tris, e non azzera
        assertThat(GameRules.practiceRun(List.of(passed(1), passed(1), passed(1)), 3))
                .isEqualTo(new PracticeRun(1, 0, 0));
        // Il giorno dopo la stessa voce conta di nuovo
        List<PracticeOutcome> twoDays = List.of(passed(1), passed(2),
                new PracticeOutcome(1, MONDAY.plusDays(1), true, true));
        assertThat(GameRules.practiceRun(twoDays, 3)).isEqualTo(new PracticeRun(0, 1, 0));
    }

    @Test
    void gliArgomentiPiuAvantiNelProgrammaNonContanoENonAzzerano() {
        List<PracticeOutcome> outcomes = List.of(passed(1), passed(2),
                new PracticeOutcome(9, MONDAY, false, false),
                new PracticeOutcome(8, MONDAY, true, false),
                passed(3));
        assertThat(GameRules.practiceRun(outcomes, 3)).isEqualTo(new PracticeRun(0, 1, 0));
    }

    @Test
    void treErroriDiFilaPortanoAllaPausa() {
        assertThat(GameRules.practiceRun(List.of(passed(1), failed(2), failed(3), failed(4)), 3).failedInARow())
                .isEqualTo(3);
        // Un esercizio superato interrompe la fila di errori
        assertThat(GameRules.practiceRun(List.of(failed(2), failed(3), passed(1)), 3).failedInARow()).isZero();
    }

    @Test
    void laSfidaContaLeVociDiverseSuperateOggi() {
        List<PracticeOutcome> outcomes = List.of(
                new PracticeOutcome(1, MONDAY.minusDays(1), true, true),
                passed(1), passed(1), passed(2), failed(3),
                new PracticeOutcome(7, MONDAY, true, false));
        assertThat(GameRules.passedToday(outcomes, MONDAY)).isEqualTo(2);
    }
}
