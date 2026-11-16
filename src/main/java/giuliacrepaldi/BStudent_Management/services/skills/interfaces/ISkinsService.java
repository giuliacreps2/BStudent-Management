package giuliacrepaldi.BStudent_Management.services.skills.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterAdminDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterAdminResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterStageDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.RosterCharacterDTO;

import java.util.List;

/**
 * Personaggi (nome tecnico "skin"): scelta, cambio e gestione Admin
 * (blueprint principale §11.4; Blueprint Gamification §4, §5).
 * Nell'interfaccia si dice solo "Personaggio" ed "Evoluzioni", mai "skin".
 * Alla registrazione lo studente sceglie uno dei personaggi iniziali e può cambiarlo liberamente; gli altri
 * restano "Prossimamente". Quali sono iniziali lo indica l'Admin: il numero non è fisso nel codice (§5.4).
 * Lo stadio è dello studente (IStudentXpService); ogni personaggio ha le sue immagini per ogni stadio.
 *
 * <p>Roadmap (dopo la beta): sblocco degli altri personaggi, evoluzioni proprie di ogni personaggio,
 * personalizzazione dell'aspetto.
 */
public interface ISkinsService {

    // Personaggi pubblicati: in uso / iniziale / "Prossimamente"
    List<RosterCharacterDTO> getRoster(long studentId);

    // Solo un personaggio iniziale pubblicato; vale per la prima scelta e per il cambio.
    // Lo stadio mostrato resta quello dello studente
    CharacterDTO chooseCharacter(long studentId, long characterId);

    // Personaggio in uso con i suoi stadi. Studente senza scelta: il primo personaggio iniziale.
    // Nessun personaggio pubblicato: null
    CharacterDTO getCurrentCharacter(long studentId);

    // Admin
    List<CharacterAdminResponseDTO> findAll();

    CharacterAdminResponseDTO findById(long characterId);

    CharacterAdminResponseDTO save(CharacterAdminDTO body);

    CharacterAdminResponseDTO update(long characterId, CharacterAdminDTO body);

    // Crea o aggiorna lo stadio: nome e immagine. Lo stadio deve esistere tra le regole degli stadi
    CharacterAdminResponseDTO saveStage(long characterId, int stageNumber, CharacterStageDTO body);

    // Solo se ha l'immagine di ogni stadio previsto (Base, I, II nella beta)
    CharacterAdminResponseDTO publish(long characterId);

    // Torna in bozza: possibile solo se nessuno studente lo ha scelto
    CharacterAdminResponseDTO withdraw(long characterId);

    // Solo in bozza e mai scelto da uno studente
    void delete(long characterId);
}
