package giuliacrepaldi.BStudent_Management.payloads.gamification;

// Uno stadio del personaggio. Per lo studente imageReference c'è solo se lo stadio è raggiunto:
// gli stadi futuri restano una sagoma (Blueprint Gamification §4)
public record CharacterStageResponseDTO(int stageNumber, String name, String description, boolean reached,
                                        String imageReference) {
}
