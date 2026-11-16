package giuliacrepaldi.BStudent_Management.payloads.gamification;

// Personaggio nell'elenco dello studente. status: selected (in uso), unlocked (iniziale, si può scegliere),
// locked (gli altri: unlockHint = "Prossimamente")
public record RosterCharacterDTO(Long characterId, String code, String name, String imageReference, String status,
                                 String unlockHint) {
}
