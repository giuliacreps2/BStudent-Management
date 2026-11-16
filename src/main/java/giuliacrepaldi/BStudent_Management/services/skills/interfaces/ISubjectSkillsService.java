package giuliacrepaldi.BStudent_Management.services.skills.interfaces;

import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;

import java.util.List;

/**
 * Skill scelte da ogni materia (blueprint §10.4-ter, tabella subject_skills della v0.9).
 * Catalogo unico delle skill; ogni materia sceglie le sue, in un ordine stabilito dall'autrice
 * (es. Latino con Grammatica, Lessico, Analisi, Traduzione e Metrica; Greco senza Metrica).
 * Punti e livelli si contano per materia (gamification, altro branch).
 * <p>
 * - La skill predefinita della materia entra da sola tra le sue skill (ISubjectsService) e non si toglie.
 * - Una skill usata da voci della Palestra o da pesi di esercizi delle missioni della materia non si toglie.
 * - assertSkillOfSubject è il controllo unico: lo usano i pesi degli esercizi di missione e le voci della Palestra.
 */
public interface ISubjectSkillsService {

    //Admin

    List<SubjectSkill> findBySubject(long subjectId); // nell'ordine dell'autrice

    List<SubjectSkill> addSkill(long subjectId, long skillId); // in coda; già presente -> ConflictException

    // ConflictException se è la skill predefinita o se è usata nella materia (il messaggio dice dove)
    List<SubjectSkill> removeSkill(long subjectId, long skillId);

    // Tutti e soli gli ID delle skill della materia, nel nuovo ordine: atomico con SET CONSTRAINTS DEFERRED
    List<SubjectSkill> reorderSkills(long subjectId, List<Long> skillIds);

    void assertSkillOfSubject(long subjectId, long skillId); // BadRequestException se la materia non la usa
}
