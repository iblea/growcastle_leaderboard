package parser.schedule;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ParseSchedularTest {

    @Test
    void now15MinutesTest() {
        // 2023-10-2 15:20:03
        LocalDateTime specificDateTime = LocalDateTime.of(2023, Month.OCTOBER, 2, 15, 20, 3);

        ParseSchedular parseSchedular = new ParseSchedular(null, null);
        LocalDateTime divide15Minutes = parseSchedular.divideHour(specificDateTime);

        // 2023-10-2 15:15:00
        // assertThat(divide15Minutes).isEqualTo(LocalDateTime.of(2023, Month.OCTOBER, 2, 15, 15, 0));
        assertThat(divide15Minutes).isEqualTo(LocalDateTime.of(2023, Month.OCTOBER, 2, 15, 0, 0));
    }

    @Test
    // 0 나누기 테스트
    void nowDivideHourTest_0() {
        // 2023-10-2 15:00:03
        LocalDateTime specificDateTime = LocalDateTime.of(2023, Month.OCTOBER, 2, 15, 0, 3);
        LocalDateTime plus15Minutes = specificDateTime.plusSeconds(900);

        ParseSchedular parseSchedular = new ParseSchedular(null, null);
        LocalDateTime divide15Minutes1 = parseSchedular.divideHour(specificDateTime);
        LocalDateTime divide15Minutes2 = parseSchedular.divideHour(plus15Minutes);

        // 2023-10-2 15:00:00
        assertThat(divide15Minutes1).isEqualTo(LocalDateTime.of(2023, Month.OCTOBER, 2, 15, 0, 0));
        // 2023-10-2 15:15:00
        assertThat(divide15Minutes2).isEqualTo(LocalDateTime.of(2023, Month.OCTOBER, 2, 15, 0, 0));
    }

    @Test
    void nowDivideHourTest_Plus15Minutes_Change_Hour() {
        // 2023-10-2 14:45:03
        LocalDateTime specificDateTime = LocalDateTime.of(2023, Month.OCTOBER, 2, 14, 45, 3);
        LocalDateTime plus15Minutes = specificDateTime.plusSeconds(900);

        ParseSchedular parseSchedular = new ParseSchedular(null, null);
        LocalDateTime divide15Minutes = parseSchedular.divideHour(plus15Minutes);

        // 2023-10-2 15:00:00
        assertThat(divide15Minutes).isEqualTo(LocalDateTime.of(2023, Month.OCTOBER, 2, 15, 0, 0));
    }


    @Test
    void nowDivideHourTest_Plus15Minutes_Change_Hour_2() {
        // 2023-10-2 14:45:03
        LocalDateTime specificDateTime = LocalDateTime.of(2023, Month.OCTOBER, 2, 14, 46, 3);
        LocalDateTime plus15Minutes = specificDateTime.plusSeconds(900);

        ParseSchedular parseSchedular = new ParseSchedular(null, null);
        LocalDateTime divide15Minutes = parseSchedular.divideHour(plus15Minutes);

        // 2023-10-2 15:00:00
        assertThat(divide15Minutes).isEqualTo(LocalDateTime.of(2023, Month.OCTOBER, 2, 15, 0, 0));
    }

}