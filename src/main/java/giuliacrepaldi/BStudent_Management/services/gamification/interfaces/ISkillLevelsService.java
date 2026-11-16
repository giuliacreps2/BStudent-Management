package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

import giuliacrepaldi.BStudent_Management.entities.skills.SkillLevel;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeLockDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelsDTO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Livelli delle skill e sblocco delle voci della Palestra (Blueprint Gamification §2, §3).
 * Il livello si calcola dai punti dello studente in una skill e in una materia (Palestra + missioni):
 * si parte dal livello 1 con 0 punti; soglie della beta 0, 20, 50, 95, 165, 265. Il livello non scende mai.
 * Una voce è aperta se la sua difficoltà è al massimo "livello della skill + 1", nella materia della voce.
 * Una sola scala di difficoltà in tutta la piattaforma (§3.6).
 * Livello di partenza dal test di orientamento (§2.6): vale come punti di partenza pari alla soglia del
 * livello, usati solo per il livello; per gli accessori contano i punti veri.
 * I calcoli stanno in utilities.GameRules (coperti dai test); qui si leggono i fatti dalla vista
 * student_subject_skill_totals sopra skill_credit_entries.
 *
 * <h2>TODO</h2>
 * <pre>
 * ProgramNoticeDTO getProgramNotice(long studentId, long practiceExerciseId);
 *     // Programma (§3-bis): "per la tua classe" oppure "Di solito si studia in [classe]". Serve la classe
 *     // dello studente, che nasce con il lavoro sull'account (Piano MVP, P.3). La classe di riferimento
 *     // dell'argomento esiste già (topics.reference_year). Le voci "più avanti" restano apribili
 * </pre>
 * TODO (letture dello studente, IStudentPracticeService): usare getLocks nell'elenco delle voci e getLock
 * prima di avviare un tentativo.
 */
public interface ISkillLevelsService {

    // Una riga per skill della materia, nell'ordine dell'autrice
    List<SkillLevelStateDTO> getLevels(long studentId, long subjectId);

    // Livello dello studente nella skill della materia (1 se non ha punti né livello di partenza)
    int getLevel(long studentId, long subjectId, long skillId);

    // Media semplice e non arrotondata dei livelli; contano solo le skill della materia con esercizi
    // pubblicati (§4.2). Nessuna skill valida: 1
    BigDecimal getAverageLevel(long studentId, long subjectId);

    // La materia migliore tra quelle in cui lo studente ha punti o un livello di partenza (§4.2)
    BigDecimal getBestAverageLevel(long studentId);

    // Punti veri nella skill, nella materia migliore: è la misura delle soglie degli accessori (§11.4)
    BigDecimal getBestPoints(long studentId, long skillId);

    PracticeLockDTO getLock(long studentId, long practiceExerciseId);

    // Stesso calcolo per un elenco, con una sola lettura dei totali
    Map<Long, PracticeLockDTO> getLocks(long studentId, Collection<Long> practiceExerciseIds);

    // Lo chiama il test di orientamento; può solo salire; livello tra 1 e il massimo della skill
    void setStartingLevel(long studentId, long subjectId, long skillId, int level);

    // Admin
    List<SkillLevel> findLevels(long skillId);

    // Soglie crescenti, la prima è 0. Si possono aggiungere livelli o abbassare le soglie, non alzarle né
    // togliere livelli: chi li aveva raggiunti scenderebbe, e il livello non scende mai (§2.3)
    List<SkillLevel> replaceLevels(long skillId, SkillLevelsDTO body);

    // Curva della beta per una skill appena creata (§2.2)
    void createDefaultLevels(long skillId);

    // Numero di livelli della skill: è anche la difficoltà massima di una voce della Palestra
    int getMaxLevel(long skillId);
}
