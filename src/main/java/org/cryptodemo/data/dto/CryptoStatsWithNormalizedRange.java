package org.cryptodemo.data.dto;

import javax.annotation.Nullable;
import java.math.BigDecimal;

public record CryptoStatsWithNormalizedRange(
        CryptoTimeRangeStats crypto,
        @Nullable BigDecimal normalizedRange
) {
}
