package org.cryptodemo.services;

import com.google.common.collect.ImmutableList;
import org.cryptodemo.dal.CryptoRepository;
import org.cryptodemo.data.CryptoName;
import org.cryptodemo.data.dto.CryptoStatsWithNormalizedRange;
import org.cryptodemo.data.dto.CryptoTimeRangeStats;
import org.cryptodemo.data.dto.PriceStats;
import org.cryptodemo.data.entity.CryptoRecord;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class CryptoServiceTest {

    @Mock
    private CryptoRepository repository;

    @InjectMocks
    private CryptoService service;

    @Test
    void getCryptoInfo() throws SQLException {
        when(repository.getCryptoRecordsTimestamps(eq(CryptoName.ETH), anyLong(), anyLong()))
                .thenReturn(ImmutableList.of(
                        new CryptoRecord(4, "ETH", new BigDecimal(4)),
                        new CryptoRecord(3, "ETH", new BigDecimal(1)),
                        new CryptoRecord(2, "ETH", new BigDecimal(5)),
                        new CryptoRecord(1, "ETH", new BigDecimal(2))));
        final CryptoTimeRangeStats cryptoTimeRangeStats = service.getCryptoInfo(CryptoName.ETH, 0);
        assertThat(cryptoTimeRangeStats, is(new CryptoTimeRangeStats(CryptoName.ETH, 1L, 4L,
                new PriceStats(new BigDecimal(2), new BigDecimal(4), new BigDecimal(1), new BigDecimal(5)))));
    }

    @Test
    void getCryptoInfo_throws() throws SQLException {
        when(repository.getCryptoRecordsTimestamps(eq(CryptoName.ETH), anyLong(), anyLong())).thenThrow(new SQLException("test"));
        assertThrows(RuntimeException.class, () -> service.getCryptoInfo(CryptoName.ETH, 0), "test");
    }

    @Test
    void getTopCryptos() throws SQLException {
        when(repository.getCryptoRecordsTimestamps(eq(CryptoName.ETH), anyLong(), anyLong()))
                .thenReturn(ImmutableList.of(
                        new CryptoRecord(5, "ETH", new BigDecimal(4)),
                        new CryptoRecord(3, "ETH", new BigDecimal(1)),
                        new CryptoRecord(2, "ETH", new BigDecimal(5)),
                        new CryptoRecord(1, "ETH", new BigDecimal(2))));
        when(repository.getCryptoRecordsTimestamps(eq(CryptoName.BTC), anyLong(), anyLong()))
                .thenReturn(ImmutableList.of(
                        new CryptoRecord(7, "BTC", new BigDecimal(3)),
                        new CryptoRecord(4, "BTC", new BigDecimal(5)),
                        new CryptoRecord(3, "BTC", new BigDecimal(8)),
                        new CryptoRecord(1, "BTC", new BigDecimal(1))));
        final List<CryptoStatsWithNormalizedRange> cryptoTimeRangeStats = service.getTopCryptos(0, 10);
        assertThat(cryptoTimeRangeStats, contains(
                new CryptoStatsWithNormalizedRange(new CryptoTimeRangeStats(CryptoName.BTC, 1L, 7L,
                        new PriceStats(new BigDecimal(1), new BigDecimal(3), new BigDecimal(1), new BigDecimal(8))),
                        new BigDecimal(7)),
                new CryptoStatsWithNormalizedRange(new CryptoTimeRangeStats(CryptoName.ETH, 1L, 5L,
                        new PriceStats(new BigDecimal(2), new BigDecimal(4), new BigDecimal(1), new BigDecimal(5))),
                        new BigDecimal(4))
        ));
    }

    @Test
    void getTopCryptos_expectNoThrow() throws SQLException {
        when(repository.getCryptoRecordsTimestamps(any(), anyLong(), anyLong())).thenThrow(new SQLException("test"));
        final List<CryptoStatsWithNormalizedRange> topCryptos = service.getTopCryptos(0, 10);
        assertThat(topCryptos, empty());
    }

    @Test
    void getTopCrypto() throws SQLException {
        when(repository.getCryptoRecordsTimestamps(eq(CryptoName.ETH), anyLong(), anyLong()))
                .thenReturn(ImmutableList.of(
                        new CryptoRecord(5, "ETH", new BigDecimal(4)),
                        new CryptoRecord(3, "ETH", new BigDecimal(1)),
                        new CryptoRecord(2, "ETH", new BigDecimal(5)),
                        new CryptoRecord(1, "ETH", new BigDecimal(2))));
        when(repository.getCryptoRecordsTimestamps(eq(CryptoName.BTC), anyLong(), anyLong()))
                .thenReturn(ImmutableList.of(
                        new CryptoRecord(7, "BTC", new BigDecimal(3)),
                        new CryptoRecord(4, "BTC", new BigDecimal(5)),
                        new CryptoRecord(3, "BTC", new BigDecimal(8)),
                        new CryptoRecord(1, "BTC", new BigDecimal(1))));
        final Optional<CryptoStatsWithNormalizedRange> cryptoTimeRangeStats = service.getTopCrypto(LocalDate.ofInstant(Instant.ofEpochMilli(10), ZoneId.systemDefault()));
        assertThat(cryptoTimeRangeStats, is(Optional.of(new CryptoStatsWithNormalizedRange(new CryptoTimeRangeStats(CryptoName.BTC, 1L, 7L,
                new PriceStats(new BigDecimal(1), new BigDecimal(3), new BigDecimal(1), new BigDecimal(8))),
                new BigDecimal(7)))));
    }

    @Test
    void getTopCrypto_noData() throws SQLException {
        when(repository.getCryptoRecordsTimestamps(any(), anyLong(), anyLong())).thenReturn(emptyList());
        final Optional<CryptoStatsWithNormalizedRange> cryptoTimeRangeStats = service.getTopCrypto(LocalDate.now());
        assertThat(cryptoTimeRangeStats, is(Optional.empty()));
    }

    @Test
    void getTopCrypto_expectNoThrow() throws SQLException {
        when(repository.getCryptoRecordsTimestamps(any(), anyLong(), anyLong())).thenThrow(new SQLException("test"));
        final Optional<CryptoStatsWithNormalizedRange> topCrypto = service.getTopCrypto(LocalDate.now());
        assertTrue(topCrypto.isEmpty());
    }
}