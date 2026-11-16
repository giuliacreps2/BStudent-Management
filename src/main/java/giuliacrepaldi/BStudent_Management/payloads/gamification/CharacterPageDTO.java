package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.util.List;

// Pagina del personaggio (blueprint §11.4): elenco dei personaggi, quello in uso con i suoi stadi,
// evoluzione e accessori. Niente "aspetto" e niente bonus
public record CharacterPageDTO(List<RosterCharacterDTO> roster, CharacterDTO character, EvolutionProgressDTO evolution,
                               List<StudentAccessoryDTO> accessories) {
}
