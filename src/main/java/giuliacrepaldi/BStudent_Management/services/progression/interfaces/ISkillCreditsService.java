package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

import giuliacrepaldi.BStudent_Management.enums.progression.SourceKind;

import java.math.BigDecimal;

/**
 * Registro degli accrediti skill (blueprint principale §10, §15).
 * Delta positivo, un accredito per evento e skill, nuovo totale = precedente + delta.
 * Unica fonte dei totali e dei livelli: nessun doppio conteggio.
 */
public interface ISkillCreditsService {

    // sourceId: voce della Palestra oppure esercizio della missione. contribution = contributo ottenuto ora.
    // Accredita solo la differenza positiva rispetto al migliore contributo già registrato per studente,
    // skill, origine e sourceId; restituisce il delta (0 se non c'è miglioramento).
    // Stesso eventId e stessa skill: nessun effetto. La materia va sempre indicata: i livelli si contano per materia
    BigDecimal creditImprovement(long studentId, long subjectId, long skillId, SourceKind sourceKind, long sourceId,
                                 BigDecimal contribution, String eventId);

    BigDecimal getBestContribution(long studentId, long skillId, SourceKind sourceKind, long sourceId);
}
