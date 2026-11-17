package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeLockDTO;

import java.math.BigDecimal;

// Voce della Palestra vista dallo studente (blueprint §7.3): l'id è quello della voce, il titolo quello per lo
// studente. status: new (mai svolta), completed (superata, almeno 60%), saved (bozza), retry (sotto il 60%),
// in_review (consegnata alla docente). points = contributo acquisito sul massimo (§10.1).
// lock = lucchetto della difficoltà (Blueprint Gamification §3); premiumLocked = lucchetto premium, distinto.
// imageReference: illustrazione dell'argomento, se c'è; altrimenti il frontend usa quella della skill.
// lastActivityAt: data ISO dell'ultima attività, null se mai svolta
public record PracticeItemDTO(Long practiceExerciseId, String title, String skillCode, Long topicId, String topicName,
                              String imageReference, Integer estimatedMinutes, BigDecimal maxPoints, BigDecimal points,
                              String status, int difficulty, PracticeLockDTO lock, boolean premiumLocked,
                              String lastActivityAt) {
}
