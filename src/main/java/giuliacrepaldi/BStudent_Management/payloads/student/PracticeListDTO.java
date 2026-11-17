package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;

import java.util.List;

// Elenco delle voci di una skill, raggruppate per argomento, con il livello dello studente in quella skill
public record PracticeListDTO(SkillRefDTO skill, SkillLevelStateDTO level, List<PracticeTopicGroupDTO> topics) {
}
