package giuliacrepaldi.BStudent_Management.services.assessment.interfaces;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionAssessedDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionListItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.TeacherAssessmentDTO;
import org.springframework.data.domain.Page;

/**
 * Pagina "Consegne da correggere" (Blueprint Svolgimento §1.5, §4). Nella beta corregge l'Admin.
 * Il giudizio è definitivo (blueprint principale §6.3).
 * TODO dopo la beta: avvisare lo studente quando arriva l'esito (oggi lo vede riaprendo la missione o la Palestra).
 */
public interface ISubmissionsService {

    // Consegne in attesa, dalla più vecchia; materia facoltativa
    Page<SubmissionListItemDTO> findPending(User corrector, Long subjectId, int page, int size);

    // Una consegna in attesa o già corretta. Se lo studente ha annullato l'invio: ConflictException
    SubmissionDetailDTO getSubmission(User corrector, long exerciseAttemptId);

    // Percentuale 0-100 e commento; solo consegne in attesa (annullata o già corretta -> ConflictException).
    // Poi gli effetti: Palestra -> IPracticeProgressService.onAssessed; missione -> IMissionRunsService.onTeacherAssessed
    SubmissionAssessedDTO assess(User corrector, long exerciseAttemptId, TeacherAssessmentDTO body);

    // La regola "chi può correggere" in un punto solo: oggi l'Admin.
    // TODO importante dopo la beta: ruolo docente separato, con le sue consegne (Svolgimento §4.2)
    boolean canAssess(User user);
}
