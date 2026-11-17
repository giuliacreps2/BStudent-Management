package giuliacrepaldi.BStudent_Management.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

// Calcoli dello svolgimento senza DB (blueprint principale §6.2, §9, §10; Blueprint Svolgimento v0.1):
// correzione automatica, tempo dell'esercizio in missione, punti. PlayRulesTest li protegge
public final class PlayRules {

    private PlayRules() {
    }

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    public static final BigDecimal FULL = HUNDRED.setScale(2);
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    // ------------------------------------------------------------------ Correzione automatica (§6.2)

    // Scelta multipla: una sola risposta giusta, tutto o niente
    public static BigDecimal gradeChoice(Long selectedAnswerId, Long correctAnswerId) {
        return selectedAnswerId != null && selectedAnswerId.equals(correctAnswerId) ? FULL : ZERO;
    }

    // Completamento e traduzione di frase con alternative: giusto se coincide con una delle risposte accettate
    public static BigDecimal gradeText(String response, Collection<String> acceptedAnswers) {
        String given = normalize(response);
        if (given.isEmpty()) {
            return ZERO;
        }
        return acceptedAnswers.stream().anyMatch(accepted -> normalize(accepted).equals(given)) ? FULL : ZERO;
    }

    // Si ignorano maiuscole, spazi esterni e spazi doppi; tutto il resto deve essere preciso
    // (§6.2; Blueprint Svolgimento §10). La forma Unicode viene uniformata, così una lettera accentata
    // scritta in due modi diversi dalla tastiera resta la stessa lettera
    static String normalize(String text) {
        return text == null ? "" : Normalizer.normalize(text, Normalizer.Form.NFC).strip()
                .replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public static boolean isPassed(BigDecimal percentage, BigDecimal passingThreshold) {
        return percentage.compareTo(passingThreshold) >= 0;
    }

    // ------------------------------------------------------------------ Tempo dell'esercizio in missione (§9)

    // remainingMs = tempo rimasto adesso; disconnected = le sincronizzazioni si sono interrotte: il server
    // considera la missione salvata, senza togliere altro tempo né vite (§9.2, nota tecnica)
    public record TimerState(long remainingMs, boolean expired, boolean disconnected) {
    }

    // remainingAtMarkMs = tempo rimasto all'ultimo segno; markAt = ultimo segno (apertura dell'esercizio,
    // ripresa o ultima sincronizzazione), null se il tempo è fermo (missione salvata o consegnata).
    // Oltre la tolleranza senza sincronizzazioni conta solo la tolleranza: chiudere il browser equivale a
    // "Salva missione", che è sempre permesso
    public static TimerState timer(long remainingAtMarkMs, OffsetDateTime markAt, OffsetDateTime now, long toleranceMs) {
        if (markAt == null) {
            return new TimerState(remainingAtMarkMs, remainingAtMarkMs <= 0, false);
        }
        long elapsed = Math.max(0, Duration.between(markAt, now).toMillis());
        boolean disconnected = elapsed > toleranceMs;
        long remaining = Math.max(0, remainingAtMarkMs - Math.min(elapsed, toleranceMs));
        return new TimerState(remaining, remaining == 0, disconnected);
    }

    // ------------------------------------------------------------------ Punti (§10.1, §10.3; Svolgimento §5)

    // Palestra: punti massimi x percentuale / 100, a due decimali; sotto la soglia nessun contributo
    public static BigDecimal practiceContribution(BigDecimal maxPoints, BigDecimal percentage, BigDecimal passingThreshold) {
        if (!isPassed(percentage, passingThreshold)) {
            return ZERO;
        }
        return maxPoints.multiply(percentage).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    // Missione: punti dell'esercizio x quota della skill x percentuale x fattore delle missioni.
    // Vale anche sotto la soglia: i punti skill maturati restano se la missione non viene superata (§10.3)
    public static BigDecimal missionSkillContribution(BigDecimal maxPoints, BigDecimal weight, BigDecimal percentage,
                                                      BigDecimal factor) {
        return maxPoints.multiply(weight).multiply(percentage).multiply(factor)
                .divide(HUNDRED.multiply(HUNDRED), 2, RoundingMode.HALF_UP);
    }

    // Un nuovo risultato accredita solo la differenza positiva rispetto al migliore già ottenuto
    public static BigDecimal improvement(BigDecimal bestSoFar, BigDecimal contribution) {
        BigDecimal delta = contribution.subtract(bestSoFar);
        return delta.signum() > 0 ? delta : ZERO;
    }

    // Un esercizio della missione: punti massimi e percentuale ottenuta
    public record Scored(BigDecimal maxPoints, BigDecimal percentage) {
    }

    // Risultato della missione: media degli esercizi, dove un esercizio da più punti pesa di più.
    // Nessun esercizio: 100 (missione di sole lezioni, o esercizi tutti ritirati)
    public static BigDecimal missionPercentage(List<Scored> exercises) {
        BigDecimal totalPoints = exercises.stream().map(Scored::maxPoints).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPoints.signum() == 0) {
            return FULL;
        }
        BigDecimal weighted = exercises.stream()
                .map(exercise -> exercise.maxPoints().multiply(exercise.percentage()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return weighted.divide(totalPoints, 2, RoundingMode.HALF_UP);
    }

    // Media del corso: i migliori risultati delle missioni, con lo stesso peso per ogni missione (§10.4)
    public static BigDecimal courseAverage(Collection<BigDecimal> bestMissionPercentages) {
        if (bestMissionPercentages.isEmpty()) {
            return null;
        }
        BigDecimal sum = bestMissionPercentages.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(bestMissionPercentages.size()), 2, RoundingMode.HALF_UP);
    }
}
