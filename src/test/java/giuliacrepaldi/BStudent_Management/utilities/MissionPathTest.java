package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.utilities.MissionPath.Position;
import giuliacrepaldi.BStudent_Management.utilities.MissionPath.Step;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Missione tipo: lezione 1, esercizio 10, lezione 2, lezione 3, esercizio 20
class MissionPathTest {

    private static final List<Step> STEPS = List.of(
            new Step(false, 1), new Step(true, 10), new Step(false, 2), new Step(false, 3), new Step(true, 20));

    @Test
    void unPercorsoNuovoParteDallaPrimaLezione() {
        Position position = MissionPath.of(STEPS, Set.of(), Set.of());
        assertThat(position.currentIndex()).isZero();
        assertThat(position.statuses()).containsExactly("current", "todo", "todo", "todo", "todo");
    }

    @Test
    void dopoUnErroreSiRipartedallEsercizioNonSuperato() {
        // Lezione 1 letta, esercizio 10 fallito: il passo attuale è ancora l'esercizio
        Position position = MissionPath.of(STEPS, Set.of(1L), Set.of());
        assertThat(position.currentIndex()).isEqualTo(1);
    }

    @Test
    void leLezioniDiSeguitoSiLeggonoUnaAllaVolta() {
        Position position = MissionPath.of(STEPS, Set.of(1L, 2L), Set.of(10L));
        assertThat(position.currentIndex()).isEqualTo(3);
        assertThat(position.statuses()).containsExactly("done", "done", "done", "current", "todo");
    }

    @Test
    void conTuttiIPassiFattiLaMissioneECompletata() {
        Position position = MissionPath.of(STEPS, Set.of(1L, 2L, 3L), Set.of(10L, 20L));
        assertThat(position.complete()).isTrue();
    }

    @Test
    void unEsercizioRitiratoNonEPiuTraIPassiEVieneSaltato() {
        // L'esercizio 20 è stato ritirato: i passi visibili finiscono con la lezione 3
        List<Step> visible = STEPS.subList(0, 4);
        assertThat(MissionPath.of(visible, Set.of(1L, 2L, 3L), Set.of(10L)).complete()).isTrue();
    }

    @Test
    void unaLezioneAggiuntaPrimaDelPuntoRaggiuntoVaLetta() {
        List<Step> withNewLesson = List.of(new Step(false, 1), new Step(false, 99), new Step(true, 10), new Step(false, 2));
        Position position = MissionPath.of(withNewLesson, Set.of(1L, 2L), Set.of(10L));
        assertThat(position.currentIndex()).isEqualTo(1);
        assertThat(position.statuses()).containsExactly("done", "current", "done", "done");
    }
}
