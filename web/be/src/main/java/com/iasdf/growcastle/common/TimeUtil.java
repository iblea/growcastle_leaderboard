package com.iasdf.growcastle.common;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtil {
    private static ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    public static LocalDateTime getNow() {
        return LocalDateTime.now(KST_ZONE_ID);
    }

}