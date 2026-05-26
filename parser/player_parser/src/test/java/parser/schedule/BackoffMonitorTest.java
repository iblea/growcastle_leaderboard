package parser.schedule;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BackoffMonitorTest {

    private static final LocalDateTime BASE_TIME =
            LocalDateTime.of(2024, Month.JANUARY, 1, 12, 0, 0);

    @Nested
    @DisplayName("정상 모드 카운팅")
    class NormalMode {

        @Test
        @DisplayName("T1: 초기 상태 - count=0, 백오프 모드 아님, shouldEnterBackoff=false")
        void initial() {
            BackoffMonitor m = new BackoffMonitor();
            assertThat(m.getConsecutiveFailCount()).isZero();
            assertThat(m.isInBackoffMode()).isFalse();
            assertThat(m.shouldEnterBackoff()).isFalse();
            assertThat(m.getNextRetryTime()).isNull();
        }

        @Test
        @DisplayName("T2: recordFailure 1회 - count=1, 반환값=1, 임계값 미도달")
        void singleFailure() {
            BackoffMonitor m = new BackoffMonitor();
            int returned = m.recordFailure();

            assertThat(returned).isEqualTo(1);
            assertThat(m.getConsecutiveFailCount()).isEqualTo(1);
            assertThat(m.shouldEnterBackoff()).isFalse();
        }

        @Test
        @DisplayName("T3: recordFailure 9회 - count=9, 임계값 미도달")
        void nineFailures() {
            BackoffMonitor m = new BackoffMonitor();
            for (int i = 0; i < 9; i++) m.recordFailure();

            assertThat(m.getConsecutiveFailCount()).isEqualTo(9);
            assertThat(m.shouldEnterBackoff()).isFalse();
        }

        @Test
        @DisplayName("T4-pre: recordFailure 10회 - 임계값 도달, shouldEnterBackoff=true")
        void tenFailures() {
            BackoffMonitor m = new BackoffMonitor();
            for (int i = 0; i < 10; i++) m.recordFailure();

            assertThat(m.getConsecutiveFailCount()).isEqualTo(10);
            assertThat(m.shouldEnterBackoff()).isTrue();
        }

        @Test
        @DisplayName("recordSuccess 호출 시 count 0으로 리셋")
        void successResets() {
            BackoffMonitor m = new BackoffMonitor();
            m.recordFailure();
            m.recordFailure();
            m.recordSuccess();

            assertThat(m.getConsecutiveFailCount()).isZero();
            assertThat(m.shouldEnterBackoff()).isFalse();
        }
    }

    @Nested
    @DisplayName("백오프 모드 진입")
    class EnterBackoff {

        @Test
        @DisplayName("T4: enterBackoffMode - 상태 전환 + nextRetryTime=30초 후")
        void enterSetsState() {
            BackoffMonitor m = new BackoffMonitor();
            for (int i = 0; i < 10; i++) m.recordFailure();

            m.enterBackoffMode(BASE_TIME);

            assertThat(m.isInBackoffMode()).isTrue();
            assertThat(m.getBackoffStep()).isZero();
            assertThat(m.getBackoffStepFailCount()).isZero();
            assertThat(m.getNextRetryTime()).isEqualTo(BASE_TIME.plusSeconds(30));
        }

        @Test
        @DisplayName("백오프 모드에서 recordFailure 호출 시 카운트 변화 없음 (무시)")
        void failureIgnoredInBackoff() {
            BackoffMonitor m = new BackoffMonitor();
            for (int i = 0; i < 10; i++) m.recordFailure();
            m.enterBackoffMode(BASE_TIME);
            int before = m.getConsecutiveFailCount();

            int returned = m.recordFailure();

            assertThat(returned).isEqualTo(before);
            assertThat(m.getConsecutiveFailCount()).isEqualTo(before);
        }
    }

    @Nested
    @DisplayName("isReadyToRetry")
    class ReadyToRetry {

        @Test
        @DisplayName("정상 모드: 항상 false")
        void normalModeFalse() {
            BackoffMonitor m = new BackoffMonitor();
            assertThat(m.isReadyToRetry(BASE_TIME)).isFalse();
        }

        @Test
        @DisplayName("T5: 백오프 모드 + nextRetryTime 미도달 -> false")
        void beforeNextRetryTime() {
            BackoffMonitor m = inBackoffAt(BASE_TIME);

            assertThat(m.isReadyToRetry(BASE_TIME.plusSeconds(29))).isFalse();
        }

        @Test
        @DisplayName("백오프 모드 + nextRetryTime 정확히 도달 -> true")
        void exactlyAtNextRetryTime() {
            BackoffMonitor m = inBackoffAt(BASE_TIME);

            assertThat(m.isReadyToRetry(BASE_TIME.plusSeconds(30))).isTrue();
        }

        @Test
        @DisplayName("백오프 모드 + nextRetryTime 지남 -> true")
        void afterNextRetryTime() {
            BackoffMonitor m = inBackoffAt(BASE_TIME);

            assertThat(m.isReadyToRetry(BASE_TIME.plusSeconds(60))).isTrue();
        }
    }

    @Nested
    @DisplayName("백오프 모드 단계 진행")
    class AdvanceBackoff {

        @Test
        @DisplayName("T6: 단계 0에서 1회 실패 - stepFailCount=1, step=0 유지, nextRetry=+30초")
        void firstFailureInStep0() {
            BackoffMonitor m = inBackoffAt(BASE_TIME);
            LocalDateTime now = BASE_TIME.plusSeconds(30);

            m.advanceBackoff(now);

            assertThat(m.getBackoffStep()).isZero();
            assertThat(m.getBackoffStepFailCount()).isEqualTo(1);
            assertThat(m.getNextRetryTime()).isEqualTo(now.plusSeconds(30));
        }

        @Test
        @DisplayName("T7: 단계 0에서 3회 실패 - step=1, stepFailCount=0, nextRetry=+1분")
        void threeFailuresAdvanceToStep1() {
            BackoffMonitor m = inBackoffAt(BASE_TIME);
            LocalDateTime now = BASE_TIME.plusSeconds(30);

            m.advanceBackoff(now);
            m.advanceBackoff(now);
            m.advanceBackoff(now);

            assertThat(m.getBackoffStep()).isEqualTo(1);
            assertThat(m.getBackoffStepFailCount()).isZero();
            assertThat(m.getNextRetryTime()).isEqualTo(now.plusSeconds(60));
        }

        @Test
        @DisplayName("단계 1에서 3회 실패 - step=2, nextRetry=+5분")
        void threeFailuresInStep1AdvanceToStep2() {
            BackoffMonitor m = inBackoffAt(BASE_TIME);
            for (int i = 0; i < 3; i++) m.advanceBackoff(BASE_TIME);
            // 이제 step=1

            for (int i = 0; i < 3; i++) m.advanceBackoff(BASE_TIME);

            assertThat(m.getBackoffStep()).isEqualTo(2);
            assertThat(m.getNextRetryTime()).isEqualTo(BASE_TIME.plusSeconds(300));
        }

        @Test
        @DisplayName("T10: 최대 단계 4 도달 후 추가 실패 - step=4 유지, 30분 간격 유지")
        void maxStepStays() {
            BackoffMonitor m = inBackoffAt(BASE_TIME);
            // 4 * 3 = 12회 실패해야 step 4 도달
            for (int i = 0; i < 12; i++) m.advanceBackoff(BASE_TIME);
            assertThat(m.getBackoffStep()).isEqualTo(4);

            // 추가 실패 5회
            for (int i = 0; i < 5; i++) m.advanceBackoff(BASE_TIME);

            assertThat(m.getBackoffStep()).isEqualTo(4);
            assertThat(m.getNextRetryTime()).isEqualTo(BASE_TIME.plusSeconds(1800));
        }

        @Test
        @DisplayName("정상 모드에서 advanceBackoff 호출 시 무시")
        void advanceIgnoredInNormalMode() {
            BackoffMonitor m = new BackoffMonitor();

            m.advanceBackoff(BASE_TIME);

            assertThat(m.isInBackoffMode()).isFalse();
            assertThat(m.getBackoffStep()).isZero();
            assertThat(m.getNextRetryTime()).isNull();
        }
    }

    @Nested
    @DisplayName("복구")
    class Recover {

        @Test
        @DisplayName("T8: recoverFromBackoff - 모든 카운터 0, 정상 모드 복귀")
        void recoverResetsAll() {
            BackoffMonitor m = inBackoffAt(BASE_TIME);
            m.advanceBackoff(BASE_TIME);
            m.advanceBackoff(BASE_TIME);

            m.recoverFromBackoff();

            assertThat(m.isInBackoffMode()).isFalse();
            assertThat(m.getBackoffStep()).isZero();
            assertThat(m.getBackoffStepFailCount()).isZero();
            assertThat(m.getConsecutiveFailCount()).isZero();
            assertThat(m.getNextRetryTime()).isNull();
        }

        @Test
        @DisplayName("T9: 복구 후 다시 실패 - 정상 모드 카운트 1부터 시작")
        void recoverThenFailRestartsCount() {
            BackoffMonitor m = inBackoffAt(BASE_TIME);
            m.recoverFromBackoff();

            int count = m.recordFailure();

            assertThat(count).isEqualTo(1);
            assertThat(m.getConsecutiveFailCount()).isEqualTo(1);
            assertThat(m.isInBackoffMode()).isFalse();
        }
    }

    private static BackoffMonitor inBackoffAt(LocalDateTime entryTime) {
        BackoffMonitor m = new BackoffMonitor();
        for (int i = 0; i < BackoffMonitor.BACKOFF_THRESHOLD; i++) {
            m.recordFailure();
        }
        m.enterBackoffMode(entryTime);
        return m;
    }
}
