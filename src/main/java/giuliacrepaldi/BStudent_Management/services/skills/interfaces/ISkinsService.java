package giuliacrepaldi.BStudent_Management.services.skills.interfaces;

/**
 * Personaggi (nome tecnico "skin"): scelta, cambio e gestione Admin
 * (blueprint principale §11.4; Blueprint Gamification §4, §5).
 * Nell'interfaccia si dice solo "Personaggio" ed "Evoluzioni", mai "skin".
 * Alla registrazione lo studente sceglie uno dei personaggi iniziali e può cambiarlo liberamente; gli altri
 * restano "Prossimamente". Quali sono iniziali lo indica l'Admin: il numero non è fisso nel codice (§5.4).
 * Lo stadio è dello studente (IStudentXpService); ogni personaggio ha le sue immagini per ogni stadio.
 *
 * <h2>TODO — metodi da implementare (Fase D)</h2>
 * <pre>
 * List&lt;RosterCharacterDTO&gt; getRoster(long studentId);
 *     // personaggi pubblicati: scelto / disponibile (iniziale) / "Prossimamente" (non iniziale)
 * RosterCharacterDTO chooseCharacter(long studentId, long skinId);
 *     // solo un personaggio iniziale pubblicato, altrimenti BadRequestException; vale per la prima
 *     // scelta e per il cambio; lo stadio mostrato resta quello dello studente
 * CharacterDTO getCurrentCharacter(long studentId);
 *     // nome, motto facoltativo, immagine dello stadio attuale. Studente senza scelta (registrato prima
 *     // di questa regola): il primo personaggio iniziale
 *
 * // Admin
 * Skin save(CharacterAdminDTO body);
 * Skin update(long skinId, CharacterAdminDTO body);
 *     // nome, descrizione, motto, "personaggio iniziale", stato
 * SkinEvolution saveStage(long skinId, CharacterStageDTO body);
 *     // numero dello stadio, nome, immagine (Cloudinary)
 * Skin publish(long skinId);
 *     // solo se ha un'immagine per ogni stadio previsto (Base, I, II nella beta)
 * EvolutionRulesDTO getEvolutionRules();
 * EvolutionRulesDTO updateEvolutionRules(EvolutionRulesDTO body);
 *     // soglie uguali per tutti i personaggi: XP e livello medio minimo per stadio (800 e 3 per il II)
 * </pre>
 * DTO da creare: RosterCharacterDTO, CharacterDTO, CharacterAdminDTO, CharacterStageDTO, EvolutionRulesDTO.
 * Controller: /student/character, /admin/academy/characters.
 * Roadmap (dopo la beta): sblocco degli altri personaggi, evoluzioni proprie di ogni personaggio,
 * personalizzazione dell'aspetto.
 */
public interface ISkinsService {
}
