package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;

// Skill della materia nella pagina "Esercizi": dati della skill, livello dello studente e numero di voci visibili
public record PracticeSkillDTO(SkillRefDTO skill, SkillLevelStateDTO level, int itemsCount) {
}
