package giuliacrepaldi.BStudent_Management.services.skills;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.skills.CharacterSelection;
import giuliacrepaldi.BStudent_Management.entities.skills.EvolutionStageRule;
import giuliacrepaldi.BStudent_Management.entities.skills.Skin;
import giuliacrepaldi.BStudent_Management.entities.skills.SkinEvolution;
import giuliacrepaldi.BStudent_Management.entities.skills.StudentSkin;
import giuliacrepaldi.BStudent_Management.enums.skills.EvolutionRuleKind;
import giuliacrepaldi.BStudent_Management.enums.skills.StatusSkill;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.RosterCharacterDTO;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.CharacterSelectionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.EvolutionStageRulesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkinEvolutionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkinsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentEvolutionStagesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentSkinEvolutionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentSkinsRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Personaggi (Blueprint Gamification §4, §5): due iniziali, un terzo "Prossimamente"
class SkinsServiceTest {

    private static final long STUDENT = 7L;

    private SkinsRepository skins;
    private SkinEvolutionsRepository evolutions;
    private StudentSkinsRepository studentSkins;
    private CharacterSelectionsRepository selections;
    private StudentEvolutionStagesRepository studentStages;
    private EntityManager entityManager;
    private SkinsService service;

    private final Skin marco = skin(1, "marco", "Marco", true);
    private final Skin livia = skin(2, "livia", "Livia", true);
    private final Skin ulisse = skin(3, "ulisse", "Ulisse", false);

    @BeforeEach
    void setUp() {
        skins = mock(SkinsRepository.class);
        evolutions = mock(SkinEvolutionsRepository.class);
        studentSkins = mock(StudentSkinsRepository.class);
        selections = mock(CharacterSelectionsRepository.class);
        studentStages = mock(StudentEvolutionStagesRepository.class);
        entityManager = mock(EntityManager.class);
        EvolutionStageRulesRepository rules = mock(EvolutionStageRulesRepository.class);
        when(rules.findAllByOrderByStageNumber()).thenReturn(List.of(
                new EvolutionStageRule(0, EvolutionRuleKind.start, null, null),
                new EvolutionStageRule(1, EvolutionRuleKind.first_course, null, null),
                new EvolutionStageRule(2, EvolutionRuleKind.thresholds, null, null)));
        StudentsRepository students = mock(StudentsRepository.class);
        when(students.getReferenceById(STUDENT)).thenReturn(new Student());

        when(skins.findByStatusSkinOrderByStarterDescSkinIdAsc(StatusSkill.published)).thenReturn(List.of(marco, livia, ulisse));
        for (Skin skin : List.of(marco, livia, ulisse)) {
            when(skins.findById(skin.getSkinId())).thenReturn(Optional.of(skin));
            stages(skin, 0, 1, 2);
        }
        when(selections.findById(STUDENT)).thenReturn(Optional.empty());
        when(studentSkins.save(any(StudentSkin.class))).thenAnswer(invocation -> {
            StudentSkin saved = invocation.getArgument(0);
            saved.setStudentSkinId(500L);
            return saved;
        });

        service = new SkinsService(skins, evolutions, studentSkins, mock(StudentSkinEvolutionsRepository.class),
                selections, rules, studentStages, students, entityManager);
    }

    private static Skin skin(long id, String code, String name, boolean starter) {
        return Skin.builder().skinId(id).skinCode(code).skinName(name).starter(starter)
                .statusSkin(StatusSkill.published).build();
    }

    private void stages(Skin skin, int... numbers) {
        List<SkinEvolution> list = new java.util.ArrayList<>();
        for (int number : numbers) {
            SkinEvolution evolution = SkinEvolution.builder().evolutionId(skin.getSkinId() * 10 + number).skin(skin)
                    .stageNumber(number).skinName(number == 0 ? "Base" : "Evoluzione " + "I".repeat(number))
                    .skinAssetPath(skin.getSkinCode() + "-" + number).build();
            list.add(evolution);
            when(evolutions.findBySkin_SkinIdAndStageNumber(skin.getSkinId(), number)).thenReturn(Optional.of(evolution));
        }
        when(evolutions.findBySkin_SkinIdOrderByStageNumber(skin.getSkinId())).thenReturn(list);
    }

    private void selected(Skin skin) {
        when(selections.findById(STUDENT)).thenReturn(Optional.of(CharacterSelection.builder()
                .studentSkin(StudentSkin.builder().studentSkinId(500L).skin(skin).build()).build()));
    }

    @Test
    void lElencoDistingueInUsoInizialiEProssimamente() {
        selected(livia);
        List<RosterCharacterDTO> roster = service.getRoster(STUDENT);
        assertThat(roster).extracting(RosterCharacterDTO::status).containsExactly("unlocked", "selected", "locked");
        assertThat(roster.get(2).unlockHint()).isEqualTo("Prossimamente");
        assertThat(roster.get(0).unlockHint()).isNull();
    }

    @Test
    void tuttiIPersonaggiMostranoLoStadioDelloStudente() {
        when(studentStages.findCurrentStage(STUDENT)).thenReturn(1);
        assertThat(service.getRoster(STUDENT)).extracting(RosterCharacterDTO::imageReference)
                .containsExactly("marco-1", "livia-1", "ulisse-1");
    }

    @Test
    void siSceglieSoloUnPersonaggioIniziale() {
        assertThatThrownBy(() -> service.chooseCharacter(STUDENT, ulisse.getSkinId()))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("non è ancora disponibile");

        CharacterDTO chosen = service.chooseCharacter(STUDENT, marco.getSkinId());
        assertThat(chosen.name()).isEqualTo("Marco");
        verify(entityManager).persist(any(CharacterSelection.class));
    }

    @Test
    void cambiarePersonaggioNonFaPerdereLoStadio() {
        when(studentStages.findCurrentStage(STUDENT)).thenReturn(1);
        selected(marco);

        CharacterDTO livia1 = service.chooseCharacter(STUDENT, livia.getSkinId());

        assertThat(livia1.stageNumber()).isEqualTo(1);
        assertThat(livia1.imageReference()).isEqualTo("livia-1");
        // Lo stadio II non è ancora raggiunto: resta senza immagine
        assertThat(livia1.stages().get(2).reached()).isFalse();
        assertThat(livia1.stages().get(2).imageReference()).isNull();
    }

    @Test
    void senzaSceltaSiVedeIlPrimoPersonaggioIniziale() {
        assertThat(service.getCurrentCharacter(STUDENT).code()).isEqualTo("marco");
    }

    @Test
    void nonSiPubblicaUnPersonaggioSenzaTutteLeImmagini() {
        Skin draft = Skin.builder().skinId(9L).skinCode("enea").skinName("Enea").statusSkin(StatusSkill.draft).build();
        when(skins.findById(9L)).thenReturn(Optional.of(draft));
        stages(draft, 0, 1);

        assertThatThrownBy(() -> service.publish(9L))
                .isInstanceOf(ConflictException.class).hasMessage("Manca l'immagine dello stadio 2: aggiungila prima di pubblicare");

        stages(draft, 0, 1, 2);
        assertThat(service.publish(9L).status()).isEqualTo(StatusSkill.published);
    }

    @Test
    void unPersonaggioSceltoDaUnoStudenteNonSiNasconde() {
        when(studentSkins.existsBySkin_SkinId(marco.getSkinId())).thenReturn(true);
        assertThatThrownBy(() -> service.withdraw(marco.getSkinId()))
                .isInstanceOf(ConflictException.class).hasMessageContaining("non si può nascondere");
    }
}
