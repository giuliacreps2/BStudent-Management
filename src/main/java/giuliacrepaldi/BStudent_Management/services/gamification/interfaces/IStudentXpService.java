package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

import giuliacrepaldi.BStudent_Management.entities.skills.EvolutionStageRule;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionStageRuleDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * XP dello studente ed evoluzioni del personaggio (Blueprint Gamification §1, §4).
 * XP = somma dei punti massimi degli esercizi della missione, accreditati solo al primo superamento.
 * Stadi della beta: Base, Evoluzione I (primo corso completato, base vite da 5 a 7), Evoluzione II
 * (800 XP e livello medio almeno 3 nella materia migliore). Uno stadio raggiunto non si perde mai.
 * XP e stadio sono dello studente: tutti i personaggi mostrano lo stesso stadio.
 *
 * <p>I metodi on... sono gli eventi che il Blocco 5 (svolgimento) deve chiamare: oggi nessuno li chiama.
 * Restituiscono gli stadi nuovi, così il frontend mostra "Il tuo personaggio si è evoluto" (§8b.5).
 */
public interface IStudentXpService {

    BigDecimal getTotalXp(long studentId);

    // 0 = Base
    int getCurrentStage(long studentId);

    EvolutionProgressDTO getEvolutionProgress(long studentId);

    // Primo superamento della missione: accredita gli XP (somma dei punti massimi degli esercizi visibili in
    // quel momento). Ripetizioni e doppio invio: nessun effetto. Poi valuta le evoluzioni
    List<Integer> onMissionPassed(long studentId, long missionId, long missionAttemptId);

    // Dopo ogni accredito di punti skill (Palestra o missione)
    List<Integer> onSkillPointsCredited(long studentId);

    // Primo corso completato: Evoluzione I una sola volta e base delle vite a 7
    List<Integer> onCourseCompleted(long studentId, long courseId);

    // Assegna in ordine gli stadi con soglie di cui lo studente ha i requisiti (§4.4)
    List<Integer> evaluateEvolutions(long studentId);

    // Admin: regole degli stadi, uguali per tutti i personaggi
    List<EvolutionStageRule> findRules();

    // Solo gli stadi con soglie (XP e livello medio). Abbassarle non toglie nulla a nessuno; alzarle vale
    // per chi non ha ancora raggiunto lo stadio (§4.5)
    EvolutionStageRule updateRule(int stageNumber, EvolutionStageRuleDTO body);
}
