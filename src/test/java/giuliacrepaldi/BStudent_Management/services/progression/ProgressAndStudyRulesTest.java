package giuliacrepaldi.BStudent_Management.services.progression;

import giuliacrepaldi.BStudent_Management.enums.gamification.AccessoryStatus;
import giuliacrepaldi.BStudent_Management.enums.progression.StudyEventKind;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StudentAccessoryDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.StudyEventDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.StudyTimeDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// "I miei progressi" e registro di studio (blueprint §10.2, §10.5)
class ProgressAndStudyRulesTest {

    @Test
    void ilVotoSiMostraAlMezzoPunto() {
        assertThat(ProgressQueryService.grade(new BigDecimal("80.00"))).isEqualByComparingTo("8");
        assertThat(ProgressQueryService.grade(new BigDecimal("82.40"))).isEqualByComparingTo("8");
        assertThat(ProgressQueryService.grade(new BigDecimal("83.00"))).isEqualByComparingTo("8.5");
        assertThat(ProgressQueryService.grade(new BigDecimal("87.00"))).isEqualByComparingTo("8.5");
        assertThat(ProgressQueryService.grade(new BigDecimal("88.00"))).isEqualByComparingTo("9");
        assertThat(ProgressQueryService.grade(new BigDecimal("100"))).isEqualByComparingTo("10");
        assertThat(ProgressQueryService.grade(null)).isNull();
    }

    private static StudentAccessoryDTO accessory(long id, AccessoryStatus status, String required, String current) {
        return new StudentAccessoryDTO(id, "a" + id, "Accessorio " + id, "testa", status, "Grammatica",
                new BigDecimal(required), new BigDecimal(current), null, null, null);
    }

    @Test
    void gliAccessoriViciniSonoPrimaIRiscattabiliPoiIPiuAvanti() {
        List<StudentAccessoryDTO> near = ProgressQueryService.nearAccessories(List.of(
                accessory(1, AccessoryStatus.owned, "50", "60"),
                accessory(2, AccessoryStatus.locked, "100", "20"),
                accessory(3, AccessoryStatus.locked, "100", "90"),
                accessory(4, AccessoryStatus.redeemable, "50", "55"),
                accessory(5, AccessoryStatus.locked, "100", "50")));

        assertThat(near).extracting(StudentAccessoryDTO::accessoryId).containsExactly(4L, 3L, 5L);
    }

    @Test
    void ilTempoDiStudioSommaTeoriaEdEserciziEFaLaMediaSuiGiorniDiAttivita() {
        StudyTimeDTO time = StudyEventsService.summarize(7, List.of(
                new StudyTimeDTO.Day("2026-11-20", 10, 20),
                new StudyTimeDTO.Day("2026-11-21", 5, 0)));

        assertThat(time.totalMinutes()).isEqualTo(35);
        assertThat(time.theoryMinutes()).isEqualTo(15);
        assertThat(time.exerciseMinutes()).isEqualTo(20);
        assertThat(time.activeDays()).isEqualTo(2);
        assertThat(time.averageMinutesPerActiveDay()).isEqualTo(18);
        assertThat(StudyEventsService.summarize(7, List.of()).averageMinutesPerActiveDay()).isZero();
    }

    private static StudyEventDTO reading(OffsetDateTime startedAt, Long activeMs) {
        return new StudyEventDTO(StudyEventKind.lesson_read, 1L, null, null, null, startedAt, activeMs);
    }

    @Test
    void unTrattoDiLetturaNonPuoDurarePiuDelTempoTrascorso() {
        OffsetDateTime now = OffsetDateTime.now();

        assertThat(StudyEventsService.checkReading(reading(now.minusMinutes(1), 30_000L), now))
                .isEqualTo(now.minusMinutes(1));
        assertThatThrownBy(() -> StudyEventsService.checkReading(reading(now.minusMinutes(1), 10 * 60_000L), now))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> StudyEventsService.checkReading(reading(now.plusHours(1), 1_000L), now))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> StudyEventsService.checkReading(reading(now, null), now))
                .isInstanceOf(BadRequestException.class);
    }
}
