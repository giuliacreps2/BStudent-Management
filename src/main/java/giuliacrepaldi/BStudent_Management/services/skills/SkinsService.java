package giuliacrepaldi.BStudent_Management.services.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.CharacterSelection;
import giuliacrepaldi.BStudent_Management.entities.skills.EvolutionStageRule;
import giuliacrepaldi.BStudent_Management.entities.skills.Skin;
import giuliacrepaldi.BStudent_Management.entities.skills.SkinEvolution;
import giuliacrepaldi.BStudent_Management.entities.skills.StudentSkin;
import giuliacrepaldi.BStudent_Management.entities.skills.StudentSkinEvolution;
import giuliacrepaldi.BStudent_Management.enums.skills.StatusSkill;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterAdminDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterAdminResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterStageDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterStageResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.RosterCharacterDTO;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.CharacterSelectionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.EvolutionStageRulesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkinEvolutionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkinsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentEvolutionStagesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentSkinEvolutionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentSkinsRepository;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISkinsService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SkinsService implements ISkinsService {

    private static final String COMING_SOON = "Prossimamente";

    private final SkinsRepository skinsRepository;
    private final SkinEvolutionsRepository evolutionsRepository;
    private final StudentSkinsRepository studentSkinsRepository;
    private final StudentSkinEvolutionsRepository studentSkinEvolutionsRepository;
    private final CharacterSelectionsRepository selectionsRepository;
    private final EvolutionStageRulesRepository stageRulesRepository;
    private final StudentEvolutionStagesRepository studentStagesRepository;
    private final StudentsRepository studentsRepository;
    private final EntityManager entityManager;

    public SkinsService(SkinsRepository skinsRepository, SkinEvolutionsRepository evolutionsRepository,
                        StudentSkinsRepository studentSkinsRepository,
                        StudentSkinEvolutionsRepository studentSkinEvolutionsRepository,
                        CharacterSelectionsRepository selectionsRepository,
                        EvolutionStageRulesRepository stageRulesRepository,
                        StudentEvolutionStagesRepository studentStagesRepository,
                        StudentsRepository studentsRepository, EntityManager entityManager) {
        this.skinsRepository = skinsRepository;
        this.evolutionsRepository = evolutionsRepository;
        this.studentSkinsRepository = studentSkinsRepository;
        this.studentSkinEvolutionsRepository = studentSkinEvolutionsRepository;
        this.selectionsRepository = selectionsRepository;
        this.stageRulesRepository = stageRulesRepository;
        this.studentStagesRepository = studentStagesRepository;
        this.studentsRepository = studentsRepository;
        this.entityManager = entityManager;
    }

    // ------------------------------------------------------------------ Studente

    @Override
    @Transactional(readOnly = true)
    public List<RosterCharacterDTO> getRoster(long studentId) {
        int stage = studentStagesRepository.findCurrentStage(studentId);
        Long selectedId = selectedSkinId(studentId);
        List<RosterCharacterDTO> roster = new ArrayList<>();
        for (Skin skin : skinsRepository.findByStatusSkinOrderByStarterDescSkinIdAsc(StatusSkill.published)) {
            boolean selected = skin.getSkinId().equals(selectedId);
            String status = selected ? "selected" : skin.isStarter() ? "unlocked" : "locked";
            // Tutti i personaggi mostrano lo stadio dello studente (§4.6)
            SkinEvolution shown = stageShown(evolutionsRepository.findBySkin_SkinIdOrderByStageNumber(skin.getSkinId()), stage);
            roster.add(new RosterCharacterDTO(skin.getSkinId(), skin.getSkinCode(), skin.getSkinName(),
                    shown == null ? null : shown.getSkinAssetPath(), status,
                    "locked".equals(status) ? COMING_SOON : null));
        }
        return roster;
    }

    @Override
    @Transactional
    public CharacterDTO chooseCharacter(long studentId, long characterId) {
        Skin skin = findSkin(characterId);
        if (skin.getStatusSkin() != StatusSkill.published) {
            throw new NotFoundException("Personaggio " + characterId + " non trovato");
        }
        if (!skin.isStarter()) {
            throw new BadRequestException(skin.getSkinName() + " non è ancora disponibile: scegli uno dei personaggi iniziali");
        }
        // La scelta nel DB punta allo stadio Base del personaggio: lo stadio mostrato si calcola da quello
        // dello studente, così un'evoluzione non richiede di aggiornare la scelta
        SkinEvolution base = evolutionsRepository.findBySkin_SkinIdAndStageNumber(characterId, 0)
                .orElseThrow(() -> new ConflictException(skin.getSkinName() + " non ha ancora l'immagine di partenza"));
        StudentSkin studentSkin = studentSkinsRepository.findByStudent_StudentIdAndSkin_SkinId(studentId, characterId)
                .orElseGet(() -> studentSkinsRepository.save(StudentSkin.builder()
                        .student(studentsRepository.getReferenceById(studentId))
                        .skin(skin)
                        .unlockedAt(OffsetDateTime.now())
                        .build()));
        StudentSkinEvolution.StudentSkinEvolutionId ownedId =
                new StudentSkinEvolution.StudentSkinEvolutionId(studentSkin.getStudentSkinId(), base.getEvolutionId());
        if (!studentSkinEvolutionsRepository.existsById(ownedId)) {
            // persist e non save: con la chiave composta già valorizzata save farebbe un merge
            entityManager.persist(StudentSkinEvolution.builder()
                    .id(ownedId).studentSkin(studentSkin).skinEvolution(base).skin(skin)
                    .unlockedAt(OffsetDateTime.now()).build());
            entityManager.flush();
        }
        CharacterSelection selection = selectionsRepository.findById(studentId).orElse(null);
        if (selection == null) {
            entityManager.persist(CharacterSelection.builder()
                    .student(studentsRepository.getReferenceById(studentId))
                    .studentSkin(studentSkin).evolution(base).selectedAt(OffsetDateTime.now()).build());
        } else {
            selection.setStudentSkin(studentSkin);
            selection.setEvolution(base);
            selection.setSelectedAt(OffsetDateTime.now());
        }
        entityManager.flush();
        return toCharacter(skin, studentStagesRepository.findCurrentStage(studentId));
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterDTO getCurrentCharacter(long studentId) {
        Long selectedId = selectedSkinId(studentId);
        Skin skin = selectedId != null ? findSkin(selectedId)
                // Studente senza scelta (registrato prima di questa regola): il primo personaggio iniziale
                : skinsRepository.findByStatusSkinOrderByStarterDescSkinIdAsc(StatusSkill.published).stream()
                        .filter(Skin::isStarter).findFirst().orElse(null);
        return skin == null ? null : toCharacter(skin, studentStagesRepository.findCurrentStage(studentId));
    }

    private Long selectedSkinId(long studentId) {
        return selectionsRepository.findById(studentId)
                .map(selection -> selection.getStudentSkin().getSkin().getSkinId())
                .orElse(null);
    }

    private CharacterDTO toCharacter(Skin skin, int stage) {
        List<SkinEvolution> evolutions = evolutionsRepository.findBySkin_SkinIdOrderByStageNumber(skin.getSkinId());
        SkinEvolution shown = stageShown(evolutions, stage);
        // Gli stadi non ancora raggiunti restano senza immagine: il frontend mostra la sagoma
        List<CharacterStageResponseDTO> stages = evolutions.stream()
                .map(e -> new CharacterStageResponseDTO(e.getStageNumber(), e.getSkinName(), e.getSkinDescription(),
                        e.getStageNumber() <= stage, e.getStageNumber() <= stage ? e.getSkinAssetPath() : null))
                .toList();
        return new CharacterDTO(skin.getSkinId(), skin.getSkinCode(), skin.getSkinName(), skin.getSkinDescription(),
                skin.getMotto(), shown == null ? 0 : shown.getStageNumber(), shown == null ? null : shown.getSkinName(),
                shown == null ? null : shown.getSkinAssetPath(), stages);
    }

    // Lo stadio più alto del personaggio che non supera quello dello studente
    private static SkinEvolution stageShown(List<SkinEvolution> evolutions, int stage) {
        SkinEvolution shown = null;
        for (SkinEvolution evolution : evolutions) {
            if (evolution.getStageNumber() <= stage) {
                shown = evolution;
            }
        }
        return shown;
    }

    // ------------------------------------------------------------------ Admin

    @Override
    @Transactional(readOnly = true)
    public List<CharacterAdminResponseDTO> findAll() {
        return skinsRepository.findAllByOrderBySkinId().stream().map(this::toAdmin).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterAdminResponseDTO findById(long characterId) {
        return toAdmin(findSkin(characterId));
    }

    @Override
    @Transactional
    public CharacterAdminResponseDTO save(CharacterAdminDTO body) {
        if (skinsRepository.existsBySkinCode(body.code())) {
            throw new ConflictException("Esiste già un personaggio con il codice " + body.code());
        }
        Skin skin = Skin.builder().skinCode(body.code()).statusSkin(StatusSkill.draft).build();
        applyContent(skin, body);
        return toAdmin(skinsRepository.save(skin));
    }

    @Override
    @Transactional
    public CharacterAdminResponseDTO update(long characterId, CharacterAdminDTO body) {
        Skin skin = findSkin(characterId);
        if (!skin.getSkinCode().equals(body.code())) {
            throw new BadRequestException("Il codice del personaggio non si cambia dopo la creazione");
        }
        // Togliere "iniziale" a un personaggio già scelto non lo toglie a chi lo usa:
        // semplicemente non si può più scegliere da capo
        applyContent(skin, body);
        return toAdmin(skin);
    }

    @Override
    @Transactional
    public CharacterAdminResponseDTO saveStage(long characterId, int stageNumber, CharacterStageDTO body) {
        Skin skin = findSkin(characterId);
        if (!stageRulesRepository.existsById(stageNumber)) {
            throw new BadRequestException("Lo stadio " + stageNumber + " non esiste: gli stadi previsti sono "
                    + stageRulesRepository.findAllByOrderByStageNumber().stream()
                    .map(rule -> String.valueOf(rule.getStageNumber())).toList());
        }
        SkinEvolution evolution = evolutionsRepository.findBySkin_SkinIdAndStageNumber(characterId, stageNumber)
                .orElseGet(() -> SkinEvolution.builder().skin(skin).stageNumber(stageNumber).build());
        evolution.setSkinName(body.name().strip());
        evolution.setSkinDescription(blankToNull(body.description()));
        evolution.setSkinAssetPath(body.imageReference().strip());
        evolutionsRepository.save(evolution);
        entityManager.flush();
        return toAdmin(skin);
    }

    @Override
    @Transactional
    public CharacterAdminResponseDTO publish(long characterId) {
        Skin skin = findSkin(characterId);
        if (skin.getStatusSkin() == StatusSkill.published) {
            throw new ConflictException("Il personaggio è già pubblicato");
        }
        // Nessuno studente deve vedere un'evoluzione senza disegno
        List<Integer> missing = missingStages(characterId);
        if (!missing.isEmpty()) {
            throw new ConflictException("Manca l'immagine " + (missing.size() == 1 ? "dello stadio " : "degli stadi ")
                    + missing.toString().replace("[", "").replace("]", "") + ": aggiungila prima di pubblicare");
        }
        skin.setStatusSkin(StatusSkill.published);
        return toAdmin(skin);
    }

    @Override
    @Transactional
    public CharacterAdminResponseDTO withdraw(long characterId) {
        Skin skin = findSkin(characterId);
        if (skin.getStatusSkin() != StatusSkill.published) {
            throw new ConflictException("Il personaggio non è pubblicato");
        }
        if (studentSkinsRepository.existsBySkin_SkinId(characterId)) {
            throw new ConflictException("Alcuni studenti hanno scelto " + skin.getSkinName()
                    + ": non si può nascondere. Togli la spunta \"iniziale\" per non farlo più scegliere");
        }
        skin.setStatusSkin(StatusSkill.draft);
        return toAdmin(skin);
    }

    @Override
    @Transactional
    public void delete(long characterId) {
        Skin skin = findSkin(characterId);
        if (skin.getStatusSkin() != StatusSkill.draft || studentSkinsRepository.existsBySkin_SkinId(characterId)) {
            throw new ConflictException("Si elimina solo un personaggio in bozza che nessuno studente ha scelto");
        }
        evolutionsRepository.deleteAll(evolutionsRepository.findBySkin_SkinIdOrderByStageNumber(characterId));
        entityManager.flush();
        skinsRepository.delete(skin);
    }

    private static void applyContent(Skin skin, CharacterAdminDTO body) {
        skin.setSkinName(body.name().strip());
        skin.setSkinDescription(blankToNull(body.description()));
        skin.setMotto(blankToNull(body.motto()));
        skin.setStarter(body.starter());
    }

    private CharacterAdminResponseDTO toAdmin(Skin skin) {
        return CharacterAdminResponseDTO.from(skin,
                evolutionsRepository.findBySkin_SkinIdOrderByStageNumber(skin.getSkinId()), missingStages(skin.getSkinId()));
    }

    // Stadi previsti dalle regole (uguali per tutti i personaggi) che questo personaggio non ha ancora
    private List<Integer> missingStages(long characterId) {
        List<Integer> present = evolutionsRepository.findBySkin_SkinIdOrderByStageNumber(characterId).stream()
                .map(SkinEvolution::getStageNumber).toList();
        return stageRulesRepository.findAllByOrderByStageNumber().stream()
                .map(EvolutionStageRule::getStageNumber)
                .filter(stage -> !present.contains(stage))
                .toList();
    }

    private Skin findSkin(long characterId) {
        return skinsRepository.findById(characterId)
                .orElseThrow(() -> new NotFoundException("Personaggio " + characterId + " non trovato"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
