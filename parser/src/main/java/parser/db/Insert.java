package parser.db;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Insert {


    public LocalDateTime getParseTime() {
        LocalDateTime now = LocalDateTime.now();
        int minutes = now.getMinute();
        int roundedMinutes = (minutes / 5) * 5;
        LocalDateTime roundedTime = now.withMinute(roundedMinutes)
            .withSecond(0)
            .withNano(0)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime();
        return roundedTime;
    }

    /**
     * 파싱한 현재 시간을 5분단위로 내려 Unix Time을 리턴한다. (Asia/Seoul - KST 기준)
     * @return long
     */
    public long getParseTimeUnix() {
        long unixTime = getParseTime().atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
        return unixTime;
    }
}