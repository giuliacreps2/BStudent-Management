package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.enums.gamification.AccessoryStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

// Calcoli della gamification senza DB (Blueprint Gamification v0.1): i service leggono i fatti e chiamano qui.
// Sono regole con numeri: GameRulesTest le protegge.
public final class GameRules {

    private GameRules() {
    }

    // Curva della beta, uguale per tutte le skill: ogni passo chiede circa 1,5 volte il precedente (§2.2).
    // È il punto di partenza di una skill nuova; poi le soglie si cambiano dall'Admin
    public static final List<BigDecimal> DEFAULT_LEVEL_CURVE = List.of(BigDecimal.ZERO, BigDecimal.valueOf(20),
            BigDecimal.valueOf(50), BigDecimal.valueOf(95), BigDecimal.valueOf(165), BigDecimal.valueOf(265));

    // ------------------------------------------------------------------ Livelli delle skill (§2)

    // Stato di una skill: nextMinPoints è null al livello massimo, e allora la barra è piena
    public record LevelState(int level, BigDecimal levelMinPoints, BigDecimal nextMinPoints, int progressPercent,
                             boolean maxLevel) {
    }

    // thresholds: soglie in ordine di livello, la prima è 0 (livello 1). Il livello è il più alto raggiunto.
    public static int levelFor(BigDecimal points, List<BigDecimal> thresholds) {
        int level = 1;
        for (int i = 1; i < thresholds.size(); i++) {
            if (points.compareTo(thresholds.get(i)) >= 0) {
                level = i + 1;
            }
        }
        return level;
    }

    // Il livello di partenza vale come punti pari alla sua soglia, usati solo per il livello (§2.6).
    // Un livello di partenza oltre il massimo viene portato al massimo
    public static BigDecimal levelPoints(BigDecimal earnedPoints, Integer startingLevel, List<BigDecimal> thresholds) {
        if (startingLevel == null || startingLevel <= 1) {
            return earnedPoints;
        }
        int index = Math.min(startingLevel, thresholds.size()) - 1;
        return earnedPoints.add(thresholds.get(index));
    }

    public static LevelState levelState(BigDecimal levelPoints, List<BigDecimal> thresholds) {
        int level = levelFor(levelPoints, thresholds);
        BigDecimal min = thresholds.get(level - 1);
        if (level == thresholds.size()) {
            return new LevelState(level, min, null, 100, true);
        }
        BigDecimal next = thresholds.get(level);
        int percent = levelPoints.subtract(min).multiply(BigDecimal.valueOf(100))
                .divide(next.subtract(min), 0, RoundingMode.DOWN).intValue();
        return new LevelState(level, min, next, percent, false);
    }

    // Una voce è aperta fino a "livello della skill + 1" (§3.2)
    public static boolean isUnlocked(int difficulty, int skillLevel) {
        return difficulty <= skillLevel + 1;
    }

    // Livello mostrato in "Si sblocca al livello N" (§3.3)
    public static int unlockLevel(int difficulty) {
        return Math.max(1, difficulty - 1);
    }

    // Media semplice dei livelli, troncata e mai arrotondata in su: 2,8 non vale 3 (§4.3). Nessuna skill: 1
    public static BigDecimal averageLevel(Collection<Integer> levels) {
        if (levels.isEmpty()) {
            return BigDecimal.ONE.setScale(2);
        }
        int sum = levels.stream().mapToInt(Integer::intValue).sum();
        return BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(levels.size()), 2, RoundingMode.DOWN);
    }

    // ------------------------------------------------------------------ Evoluzioni (§4)

    // Stadio con soglie: XP e livello medio, entrambi (§4.4)
    public static boolean meetsStage(BigDecimal xp, BigDecimal bestAverageLevel, BigDecimal minXp,
                                     BigDecimal minAverageLevel) {
        return xp.compareTo(minXp) >= 0 && bestAverageLevel.compareTo(minAverageLevel) >= 0;
    }

    // ------------------------------------------------------------------ Vite (§12.1 del blueprint principale)

    // Alla scadenza della ricarica il saldo torna almeno alla base; le vite extra restano
    public static int livesAfterRecharge(int lives, int baseLives) {
        return Math.max(lives, baseLives);
    }

    // Evoluzione I: nuova base, saldo portato almeno alla nuova base
    public static int livesAfterBaseRaise(int lives, int newBaseLives) {
        return Math.max(lives, newBaseLives);
    }

    // ------------------------------------------------------------------ Cariche del cappello (§11.3)

    public record ChargeState(int remaining, OffsetDateTime rechargeAt) {
    }

    // Ricarica completa quando il tempo è scaduto
    public static ChargeState chargesAfterRecharge(ChargeState state, int capacity, OffsetDateTime now) {
        if (state.rechargeAt() != null && !now.isBefore(state.rechargeAt())) {
            return new ChargeState(capacity, null);
        }
        return state;
    }

    // Un nuovo aiuto consuma una carica; all'ultima parte la ricarica. Va chiamato solo con cariche rimaste
    public static ChargeState chargesAfterUse(ChargeState state, int rechargeHours, OffsetDateTime now) {
        if (state.remaining() <= 0) {
            throw new IllegalStateException("Nessuna carica rimasta");
        }
        int remaining = state.remaining() - 1;
        return new ChargeState(remaining, remaining == 0 ? now.plusHours(rechargeHours) : state.rechargeAt());
    }

    // ------------------------------------------------------------------ Accessori (§11.1)

    // bestPoints: punti veri nella skill richiesta, nella materia migliore (§11.4)
    public static AccessoryStatus accessoryStatus(BigDecimal bestPoints, BigDecimal requiredPoints, boolean owned,
                                                  boolean equipped) {
        if (owned) {
            return equipped ? AccessoryStatus.equipped : AccessoryStatus.owned;
        }
        return bestPoints.compareTo(requiredPoints) >= 0 ? AccessoryStatus.redeemable : AccessoryStatus.locked;
    }

    // ------------------------------------------------------------------ Serie (§7.2)

    public record Streak(int current, int record) {
    }

    // studyDays: giorni (ora italiana) con almeno un esercizio superato. Un giorno di pausa non interrompe,
    // due di fila sì; il giorno di pausa non aumenta il numero. Oggi ancora senza studio non interrompe
    public static Streak streakOf(Collection<LocalDate> studyDays, LocalDate today) {
        int record = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate day : new TreeSet<>(studyDays)) {
            if (day.isAfter(today)) {
                break;
            }
            run = previous != null && ChronoUnit.DAYS.between(previous, day) <= 2 ? run + 1 : 1;
            record = Math.max(record, run);
            previous = day;
        }
        // Ultimo studio tre o più giorni fa: ieri e l'altro ieri vuoti, la Serie è interrotta
        boolean alive = previous != null && ChronoUnit.DAYS.between(previous, today) <= 2;
        return new Streak(alive ? run : 0, record);
    }

    // ------------------------------------------------------------------ Palestra: tris, sfida, pausa (§7.1, §8.1)

    // Un tentativo valutato della Palestra. inProgram = false per gli argomenti "più avanti nel programma",
    // che non contano e non azzerano (§3b.4)
    public record PracticeOutcome(long practiceExerciseId, LocalDate day, boolean passed, boolean inProgram) {
    }

    // inARow: superati di fila verso la prossima vita; livesEarned: tris completati nell'elenco
    public record PracticeRun(int inARow, int livesEarned, int failedInARow) {
    }

    // outcomes in ordine di valutazione. La stessa voce conta una volta sola al giorno: un secondo
    // superamento nello stesso giorno non conta e non azzera. Un errore azzera il conteggio
    public static PracticeRun practiceRun(List<PracticeOutcome> outcomes, int streakForLife) {
        Set<String> counted = new HashSet<>();
        int inARow = 0;
        int lives = 0;
        int failed = 0;
        for (PracticeOutcome outcome : outcomes) {
            if (!outcome.inProgram()) {
                continue;
            }
            if (!outcome.passed()) {
                inARow = 0;
                failed++;
                continue;
            }
            failed = 0;
            if (!counted.add(outcome.practiceExerciseId() + "@" + outcome.day())) {
                continue;
            }
            inARow++;
            if (inARow == streakForLife) {
                lives++;
                inARow = 0;
            }
        }
        return new PracticeRun(inARow, lives, failed);
    }

    // Sfida giornaliera: voci diverse superate nel giorno (§7.1)
    public static int passedToday(List<PracticeOutcome> outcomes, LocalDate today) {
        return (int) outcomes.stream()
                .filter(o -> o.inProgram() && o.passed() && o.day().equals(today))
                .map(PracticeOutcome::practiceExerciseId)
                .distinct()
                .count();
    }
}
