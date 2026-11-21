package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.AttemptDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.AttemptHistoryItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.CourseProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.GrowthDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.PracticeProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.WeakPointsDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Letture per "I miei progressi" (blueprint §10.5): i cinque gruppi decisi il 14/11/2026. Ogni gruppo è un
 * riassunto; il dettaglio si apre a richiesta. Solo lettura: compone i fatti già salvati, senza tabelle dedicate.
 * subjectSlug null = la prima materia pubblicata. Il selettore della materia compare solo se lo studente ne
 * segue più di una (frontend). Tempo di studio: IStudyEventsService, facoltativo nell'MVP.
 */
public interface IProgressQueryService {

    /** Filtro dello storico: tutto, solo missioni, solo Palestra */
    enum HistoryContext { all, mission, practice }

    /** Filtro dello storico: tutti, solo superati, solo non superati */
    enum HistoryOutcome { all, passed, failed }

    // Personaggio e XP (uguali per tutte le materie), cammino verso la prossima evoluzione, accessori vicini,
    // livelli delle skill per materia
    GrowthDTO getGrowth(long studentId);

    // Corsi visibili della materia: missioni superate su totali, media del corso (migliori risultati), completato
    List<CourseProgressDTO> getCourses(long studentId, String subjectSlug);

    // Voci svolte con il miglior risultato e i tentativi; quelle in correzione a parte
    PracticeProgressDTO getPractice(long studentId, String subjectSlug);

    // Ultimi 30 giorni di default (allTime = tutto); voto mostrato al mezzo punto (§10.2)
    Page<AttemptHistoryItemDTO> getHistory(long studentId, HistoryContext context, HistoryOutcome outcome,
                                           boolean allTime, int page, int size);

    // Risposte, feedback (anche della docente) e "rileggi la teoria". 404 se non è un tentativo consegnato
    // dello studente
    AttemptDetailDTO getAttempt(long studentId, long exerciseAttemptId);

    // Skill più bassa, argomenti con esercizi non ancora superati, ultimi risultati dei test
    WeakPointsDTO getWeakPoints(long studentId, String subjectSlug);
}
