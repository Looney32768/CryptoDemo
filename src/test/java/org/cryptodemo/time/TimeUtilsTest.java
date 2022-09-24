package org.cryptodemo.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {

    @Test
    void getUnixEpochMillisNMonthsBack() {
        final long ts1 = TimeUtils.getUnixEpochMillisNMonthsBack(0) / 1000;
        final long ts2 = System.currentTimeMillis() / 1000;
        assertTrue(ts2 - ts1 <= 1);
    }
}