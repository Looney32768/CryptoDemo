package org.cryptodemo.data.dto;

import org.cryptodemo.data.CryptoName;

import javax.annotation.Nullable;

public record CryptoTimeRangeStats(
        CryptoName cryptoName,
        @Nullable Long earliestTimestamp,
        @Nullable Long latestTimestamp,
        @Nullable PriceStats priceStats) {
}
