package parser.schedule;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import parser.entity.SeasonData;

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

    // ========================================================
    // 시즌 마지막 날 재시작 시나리오 검증 테스트
    // ========================================================

    @Nested
    @DisplayName("isAfterSeasonEnd - 순수 로직 테스트")
    class IsAfterSeasonEndTest {

        // endDate=23:55 → (55/10)*10 = 50 → 기준시각 23:50

        @Test
        @DisplayName("now=23:49 → false (시즌 진행 중)")
        void beforeEndTime_returnsFalse() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            LocalDateTime endDate = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 55, 0);
            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 49, 0);

            boolean result = schedular.isAfterSeasonEnd(now, endDate);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("now=23:50 → true (시즌 종료 기준 시점)")
        void atEndTime_returnsTrue() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            LocalDateTime endDate = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 55, 0);
            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 50, 0);

            boolean result = schedular.isAfterSeasonEnd(now, endDate);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("now=23:52 → true (crontab 재시작 시나리오)")
        void restartScenario_returnsTrue() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            LocalDateTime endDate = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 55, 0);
            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 52, 0);

            boolean result = schedular.isAfterSeasonEnd(now, endDate);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("now=23:55 → true (정확히 endDate 시각)")
        void atExactEndDate_returnsTrue() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            LocalDateTime endDate = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 55, 0);
            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 55, 0);

            boolean result = schedular.isAfterSeasonEnd(now, endDate);
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("checkSeasonEnd - Mockito spy 테스트")
    class CheckSeasonEndTest {

        private ParseSchedular createSpySchedular() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            ParseSchedular spySchedular = spy(schedular);
            doNothing().when(spySchedular).parseSeasonData(any());
            doNothing().when(spySchedular).deleteDatabaseUntilDate(any());
            doReturn(false).when(spySchedular).setDatabaseConnection();
            return spySchedular;
        }

        private void setSeasonData(ParseSchedular target, SeasonData data) throws Exception {
            Field field = ParseSchedular.class.getDeclaredField("seasonData");
            field.setAccessible(true);
            field.set(target, data);
        }

        @Test
        @DisplayName("seasonData 세팅 + now=23:52 → true (시즌 종료 판정)")
        void seasonDataSet_afterEnd_returnsTrue() throws Exception {
            ParseSchedular spySchedular = createSpySchedular();

            SeasonData seasonData = new SeasonData();
            seasonData.setStartDate(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0, 0));
            seasonData.setEndDate(LocalDateTime.of(2024, Month.JANUARY, 15, 23, 55, 0));
            setSeasonData(spySchedular, seasonData);

            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 52, 0);
            boolean result = spySchedular.checkSeasonEnd(now);

            assertThat(result).isTrue();
            verify(spySchedular).deleteDatabaseUntilDate(any());
        }

        @Test
        @DisplayName("seasonData 세팅 + now=23:49 → false (시즌 진행 중)")
        void seasonDataSet_beforeEnd_returnsFalse() throws Exception {
            ParseSchedular spySchedular = createSpySchedular();

            SeasonData seasonData = new SeasonData();
            seasonData.setStartDate(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0, 0));
            seasonData.setEndDate(LocalDateTime.of(2024, Month.JANUARY, 15, 23, 55, 0));
            setSeasonData(spySchedular, seasonData);

            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 49, 0);
            boolean result = spySchedular.checkSeasonEnd(now);

            assertThat(result).isFalse();
            verify(spySchedular, never()).deleteDatabaseUntilDate(any());
        }

        @Test
        @DisplayName("seasonData null(재시작) + parseSeasonData 후에도 null → true")
        void seasonDataNull_parseStillNull_returnsTrue() throws Exception {
            ParseSchedular spySchedular = createSpySchedular();

            // SeasonData 기본 생성자: startDate=null, endDate=null → isNull()=true
            SeasonData seasonData = new SeasonData();
            setSeasonData(spySchedular, seasonData);

            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 52, 0);
            boolean result = spySchedular.checkSeasonEnd(now);

            assertThat(result).isTrue();
            verify(spySchedular).parseSeasonData(now);
        }
    }

    @Nested
    @DisplayName("isAfterExitTime - 종료 시각 판정 테스트")
    class IsAfterExitTimeTest {

        // 종료 기준: 23:51:10

        @Test
        @DisplayName("23:51:09 → false (종료 전)")
        void beforeExitTime_returnsFalse() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 51, 9);
            assertThat(schedular.isAfterExitTime(now)).isFalse();
        }

        @Test
        @DisplayName("23:51:10 → true (종료 시점)")
        void atExitTime_returnsTrue() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 51, 10);
            assertThat(schedular.isAfterExitTime(now)).isTrue();
        }

        @Test
        @DisplayName("23:52:00 → true (crontab 재시작 시나리오)")
        void afterExitTime_returnsTrue() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 52, 0);
            assertThat(schedular.isAfterExitTime(now)).isTrue();
        }

        @Test
        @DisplayName("23:50:00 → false (시즌 종료 판정은 됐지만 아직 종료 시각 전)")
        void seasonEndButBeforeExitTime_returnsFalse() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            LocalDateTime now = LocalDateTime.of(2024, Month.JANUARY, 15, 23, 50, 0);
            assertThat(schedular.isAfterExitTime(now)).isFalse();
        }
    }

    @Nested
    @DisplayName("getGrowCastleData - 시즌 종료 시 exitProgram 호출 검증")
    class GetGrowCastleDataSeasonEndTest {

        private ParseSchedular createSpySchedular() {
            ParseSchedular schedular = new ParseSchedular(null, null);
            ParseSchedular spySchedular = spy(schedular);
            doNothing().when(spySchedular).parseSeasonData(any());
            doNothing().when(spySchedular).deleteDatabaseUntilDate(any());
            doReturn(false).when(spySchedular).setDatabaseConnection();
            doNothing().when(spySchedular).exitProgram(any());
            doNothing().when(spySchedular).getParseLeaderboards(anyBoolean());
            return spySchedular;
        }

        @Test
        @DisplayName("checkSeasonEnd=true + isAfterExitTime=true → exitProgram 호출")
        void seasonEnd_afterExitTime_callsExitProgram() throws Exception {
            ParseSchedular spySchedular = createSpySchedular();

            doReturn(true).when(spySchedular).checkSeasonEnd(any());
            doReturn(true).when(spySchedular).isAfterExitTime(any());

            spySchedular.getGrowCastleData();

            verify(spySchedular).exitProgram("Season End");
            verify(spySchedular).getParseLeaderboards(false);
        }

        @Test
        @DisplayName("checkSeasonEnd=true + isAfterExitTime=false → exitProgram 미호출 (leaderboard만 파싱)")
        void seasonEnd_beforeExitTime_doesNotCallExitProgram() throws Exception {
            ParseSchedular spySchedular = createSpySchedular();

            doReturn(true).when(spySchedular).checkSeasonEnd(any());
            doReturn(false).when(spySchedular).isAfterExitTime(any());

            spySchedular.getGrowCastleData();

            verify(spySchedular, never()).exitProgram(any());
            verify(spySchedular).getParseLeaderboards(false);
        }

        @Test
        @DisplayName("checkSeasonEnd=false → exitProgram 미호출 (정상 운영)")
        void seasonOngoing_doesNotCallExitProgram() throws Exception {
            ParseSchedular spySchedular = createSpySchedular();

            doReturn(false).when(spySchedular).checkSeasonEnd(any());

            spySchedular.getGrowCastleData();

            verify(spySchedular, never()).exitProgram(any());
        }
    }

}