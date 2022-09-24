package org.cryptodemo.data.entity;

import lombok.NonNull;

import java.math.BigDecimal;

public record CryptoRecord(
        long timestamp,
        String symbol,
        @NonNull BigDecimal price
) {
}
