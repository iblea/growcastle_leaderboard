package parser.schedule;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 서버 장애 시 호출 폭주와 텔레그램 알람 폭주를 막기 위한 지수 백오프 상태 머신.
 *
 * 정상 모드: 실패 시 consecutiveFailCount 증가. BACKOFF_THRESHOLD 도달 시 백오프 모드 진입.
 * 백오프 모드: nextRetryTime 도달 시점에만 헬스 체크 1회. 단계당 BACKOFF_STEP_FAIL_LIMIT 회
 *           실패 시 다음 단계로 진행. MAX_BACKOFF_STEP 도달 후엔 해당 단계 유지.
 */
public class BackoffMonitor {

    private static final Logger logger = LogManager.getLogger(BackoffMonitor.class);

    public static final int BACKOFF_THRESHOLD = 10;
    public static final int[] BACKOFF_INTERVALS_SEC = { 30, 60, 300, 600, 1800 };
    public static final int BACKOFF_STEP_FAIL_LIMIT = 3;
    public static final int MAX_BACKOFF_STEP = BACKOFF_INTERVALS_SEC.length - 1;

    private int consecutiveFailCount = 0;
    private boolean inBackoffMode = false;
    private int backoffStep = 0;
    private int backoffStepFailCount = 0;
    private LocalDateTime nextRetryTime = null;

    public int getConsecutiveFailCount() {
        return consecutiveFailCount;
    }

    public boolean isInBackoffMode() {
        return inBackoffMode;
    }

    public int getBackoffStep() {
        return backoffStep;
    }

    public int getBackoffStepFailCount() {
        return backoffStepFailCount;
    }

    public LocalDateTime getNextRetryTime() {
        return nextRetryTime;
    }

    /**
     * 정상 모드 실패 1건 기록. 백오프 모드에서는 무시한다.
     * @return 증가 후 consecutiveFailCount
     */
    public int recordFailure() {
        if (inBackoffMode) {
            return consecutiveFailCount;
        }
        consecutiveFailCount++;
        return consecutiveFailCount;
    }

    /**
     * 정상 모드 성공 시 카운트 0 리셋.
     * 백오프 모드 복구 알람 발송 후 정상 복귀에는 {@link #recoverFromBackoff()} 사용.
     */
    public void recordSuccess() {
        consecutiveFailCount = 0;
    }

    /**
     * 정상 모드에서 임계값 도달 여부 판정.
     */
    public boolean shouldEnterBackoff() {
        return !inBackoffMode && consecutiveFailCount >= BACKOFF_THRESHOLD;
    }

    /**
     * 백오프 모드 진입. 단계 0(30초)부터 시작, nextRetryTime 세팅.
     */
    public void enterBackoffMode(LocalDateTime now) {
        inBackoffMode = true;
        backoffStep = 0;
        backoffStepFailCount = 0;
        nextRetryTime = now.plusSeconds(BACKOFF_INTERVALS_SEC[0]);
        logger.info("Enter backoff mode at {}, next retry: {}", now, nextRetryTime);
    }

    /**
     * 백오프 모드에서 nextRetryTime 도달 여부.
     */
    public boolean isReadyToRetry(LocalDateTime now) {
        if (!inBackoffMode || nextRetryTime == null) {
            return false;
        }
        return !now.isBefore(nextRetryTime);
    }

    /**
     * 백오프 모드 헬스 체크 실패 시 호출. 단계당 한도 도달 시 다음 단계로 진행.
     * 단계가 MAX_BACKOFF_STEP 에 있으면 단계 유지, nextRetryTime 만 갱신.
     */
    public void advanceBackoff(LocalDateTime now) {
        if (!inBackoffMode) {
            return;
        }
        backoffStepFailCount++;
        if (backoffStepFailCount >= BACKOFF_STEP_FAIL_LIMIT) {
            if (backoffStep < MAX_BACKOFF_STEP) {
                backoffStep++;
            }
            backoffStepFailCount = 0;
        }
        nextRetryTime = now.plusSeconds(BACKOFF_INTERVALS_SEC[backoffStep]);
        logger.info("Backoff advanced. step={}, stepFailCount={}, nextRetry={}",
                backoffStep, backoffStepFailCount, nextRetryTime);
    }

    /**
     * 백오프 모드 헬스 체크 성공 시 호출. 모든 카운터 리셋, 정상 모드 복귀.
     */
    public void recoverFromBackoff() {
        inBackoffMode = false;
        backoffStep = 0;
        backoffStepFailCount = 0;
        consecutiveFailCount = 0;
        nextRetryTime = null;
        logger.info("Recovered from backoff mode");
    }
}
