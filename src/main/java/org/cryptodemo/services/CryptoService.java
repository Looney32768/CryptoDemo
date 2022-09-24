package org.cryptodemo.services;

import org.cryptodemo.dal.CryptoRepository;
import org.cryptodemo.data.CryptoName;
import org.cryptodemo.data.dto.CryptoStatsWithNormalizedRange;
import org.cryptodemo.data.dto.CryptoTimeRangeStats;
import org.cryptodemo.data.dto.PriceStats;
import org.cryptodemo.data.entity.CryptoRecord;
import org.cryptodemo.errors.DataNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.cryptodemo.time.TimeUtils.getUnixEpochMillisNMonthsBack;

@Service
public class CryptoService {

    private final CryptoRepository cryptoRepository;

    public CryptoService(final CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    /**
     * Calculates statistics for given crypto for a month period that starts {@code monthsBack + 1} months back
     * from today and ends {@code monthsBack} months back from today. If {@code monthsBack} is 0, the stats are
     * calculated for the last month.
     * @param cryptoName to get statistics for
     * @param monthsBack allows to move the time window into the past by given number of months
     * @return time window statistics for the crypto
     */
    public CryptoTimeRangeStats getCryptoInfo(final CryptoName cryptoName, final int monthsBack) {
        final long unixEpochMillisFrom = getUnixEpochMillisNMonthsBack(monthsBack + 1);
        final long unixEpochMillisUntil = getUnixEpochMillisNMonthsBack(monthsBack);
        return getCryptoInfo(cryptoName, unixEpochMillisFrom, unixEpochMillisUntil);
    }

    /**
     * Calculates statistics for all cryptos for a period between {@code unixEpochMillisFrom} and {@code unixEpochMillisUntil}.
     * Additionally, a normalized range value is calculated for each crypto as (max-min)/min over the period.
     * @param unixEpochMillisFrom starting point for stats calculation, must be less than {@code unixEpochMillisUntil}
     * @param unixEpochMillisUntil ending point for stats calculation, must be greater than {@code unixEpochMillisFrom}
     * @return list of statistics for all available cryptos ordered by normalized range value in descending order.
     */
    public List<CryptoStatsWithNormalizedRange> getTopCryptos(final long unixEpochMillisFrom, final long unixEpochMillisUntil) {
        return Stream.of(CryptoName.values())
                .map(cryptoName -> {
                    try {
                        return getCryptoInfo(cryptoName, unixEpochMillisFrom, unixEpochMillisUntil);
                    } catch (RuntimeException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(stats -> new CryptoStatsWithNormalizedRange(stats,
                        stats.priceStats() == null || stats.priceStats().min().equals(BigDecimal.ZERO)
                                ? null
                                : (stats.priceStats().max().subtract(stats.priceStats().min())
                                .divide(stats.priceStats().min(), RoundingMode.HALF_UP))))
                .filter(statsWithRange -> statsWithRange.normalizedRange() != null)
                .sorted(comparing(CryptoStatsWithNormalizedRange::normalizedRange).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Finds crypto with highest normalized range value calculated for the given date (between start of the
     * specified day and start of the next day).
     * @param date for finding crypto with highest normalized range value
     * @return optional of stats for the crypto or {@code Optional.empty()} if no data for the given date is available
     */
    public Optional<CryptoStatsWithNormalizedRange> getTopCrypto(final LocalDate date) {
        final long unixEpochMillisFrom = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() * 1000;
        final long unixEpochMillisUntil = date.atStartOfDay().plusDays(1).atZone(ZoneId.systemDefault()).toInstant().getEpochSecond() * 1000;
        final List<CryptoStatsWithNormalizedRange> topCryptos = getTopCryptos(unixEpochMillisFrom, unixEpochMillisUntil);
        return topCryptos.isEmpty() ? Optional.empty() : Optional.of(topCryptos.get(0));
    }

    private CryptoTimeRangeStats getCryptoInfo(final CryptoName cryptoName, final long from, final long until) {
        try {
            final List<CryptoRecord> records = cryptoRepository.getCryptoRecordsTimestamps(cryptoName, from, until);
            if (records.isEmpty()) {
                return new CryptoTimeRangeStats(cryptoName, null, null, null);
            }
            BigDecimal minPrice = records.get(0).price(), maxPrice = minPrice;
            for (CryptoRecord record : records) {
                final BigDecimal price = record.price();
                if (price.compareTo(maxPrice) > 0) {
                    maxPrice = price;
                } else if (price.compareTo(minPrice) < 0) {
                    minPrice = price;
                }
            }
            final CryptoRecord oldest = records.get(records.size() - 1);
            final CryptoRecord newest = records.get(0);
            return new CryptoTimeRangeStats(cryptoName,
                    oldest.timestamp(),
                    newest.timestamp(),
                    new PriceStats(
                            oldest.price(),
                            newest.price(),
                            minPrice,
                            maxPrice));
        } catch (SQLException e) {
            if (e.getMessage().contains("File not found")) {
                throw new DataNotFoundException("No data exists for " + cryptoName);
            } else {
                throw new RuntimeException("Error reading data for " + cryptoName);
            }
        }
    }
}
