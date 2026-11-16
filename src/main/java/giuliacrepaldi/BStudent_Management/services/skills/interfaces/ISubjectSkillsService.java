package giuliacrepaldi.BStudent_Management.services.skills.interfaces;

/**
 * Skill scelte da ogni materia (blueprint §10.4-ter, tabella subject_skills della v0.9).
 * Catalogo unico delle skill; ogni materia sceglie le sue, in un ordine stabilito dall'autrice
 * (es. Latino con Grammatica, Lessico, Analisi, Traduzione e Metrica; Greco senza Metrica).
 * Punti e livelli si contano per materia (gamification, altro branch).
 *
 * <h2>TODO — metodi da implementare (Fase 1, Admin)</h2>
 * <pre>
 * List&lt;SubjectSkill&gt; findBySubject(long subjectId);
 *     // nell'ordine dell'autrice
 * List&lt;SubjectSkill&gt; addSkill(long subjectId, long skillId);
 *     // in coda; già presente -> ConflictException
 * List&lt;SubjectSkill&gt; removeSkill(long subjectId, long skillId);
 *     // ConflictException se è la skill predefinita della materia o se è usata da voci della Palestra
 *     // o da pesi di esercizi delle missioni della materia (messaggio che dice dove)
 * List&lt;SubjectSkill&gt; reorderSkills(long subjectId, List&lt;Long&gt; skillIds);
 *     // tutti e soli gli ID, atomico con SET CONSTRAINTS subject_skills_subject_id_sort_order_idx DEFERRED
 * void assertSkillOfSubject(long subjectId, long skillId);
 *     // BadRequestException se la skill non è della materia. Da chiamare in: pesi degli esercizi di
 *     // missione (IMissionExercisesService), voci della Palestra, skill predefinita della materia (ISubjectsService)
 * </pre>
 * Entity da creare: SubjectSkill (chiave composta subject_id + skill_id, sort_order).
 * Controller: /admin/academy/subjects/{subjectId}/skills.
 */
public interface ISubjectSkillsService {
}
