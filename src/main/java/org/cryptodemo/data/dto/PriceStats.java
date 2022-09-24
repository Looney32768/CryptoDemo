package org.cryptodemo.data.dto;

import java.math.BigDecimal;

public record PriceStats(
        BigDecimal oldest,
        BigDecimal newest,
        BigDecimal min,
        BigDecimal max
) {
}
