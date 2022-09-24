package org.cryptodemo.time;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class TimeUtils {

    /**
     * Calculates milliseconds since Unix Epoch for point in time N months back from now.
     */
    public static long getUnixEpochMillisNMonthsBack(final int monthsBack) {
        return LocalDateTime.now().minusMonths(monthsBack).atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() * 1000;
    }
}
