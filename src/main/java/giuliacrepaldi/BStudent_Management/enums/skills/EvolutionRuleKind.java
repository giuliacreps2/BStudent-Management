package giuliacrepaldi.BStudent_Management.enums.skills;

// Come si raggiunge uno stadio (Blueprint Gamification §4.1)
public enum EvolutionRuleKind {
    // Stadio di partenza
    start,
    // Primo corso completato: regola a sé, porta la base delle vite a 7
    first_course,
    // XP minimi e livello medio minimo nella materia migliore
    thresholds
}
