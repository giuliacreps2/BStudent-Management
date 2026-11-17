package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

/**
 * Registro degli accrediti skill (blueprint principale §10, §15).
 * Delta positivo, un accredito per evento e skill, nuovo totale = precedente + delta.
 * Unica fonte dei totali e dei livelli: nessun doppio conteggio.
 *
 * <h2>TODO — metodi da implementare (Blocco 5)</h2>
 * <pre>
 * BigDecimal creditImprovement(long studentId, long subjectId, long skillId, SourceKind sourceKind, long sourceId,
 *                              BigDecimal contribution, String eventId);
 *     // sourceId: voce della Palestra oppure esercizio della missione. contribution = contributo ottenuto ora.
 *     // Accredita solo la differenza positiva rispetto al migliore contributo già registrato per studente,
 *     // skill, origine e sourceId; restituisce il delta (0 se non c'è miglioramento).
 *     // Stesso eventId e stessa skill: nessun effetto (vincolo unico). La materia va sempre indicata
 * BigDecimal getBestContribution(long studentId, long skillId, SourceKind sourceKind, long sourceId);
 * </pre>
 */
public interface ISkillCreditsService {
}
