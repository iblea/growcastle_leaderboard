package parser.db;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Insert {

    public Insert() {

    }

    /**
     * 파싱한 현재 시간을 5분단위로 내려 Unix Time을 리턴한다. (Asia/Seoul - KST 기준)
     * @return long
     */
    private long getParseTimeUnix() {
        LocalDateTime now = LocalDateTime.now();
        int minutes = now.getMinute();
        int roundedMinutes = (minutes / 5) * 5;
        LocalDateTime roundedTime = now.withMinute(roundedMinutes).withSecond(0).withNano(0);
        // UTC
        // long unixTime = roundedTime.toEpochSecond(java.time.ZoneOffset.UTC);
        // Asia/Seoul - KST
        long unixTime = roundedTime.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
        return unixTime;
    }
}