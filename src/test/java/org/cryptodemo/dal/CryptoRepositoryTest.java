package org.cryptodemo.dal;

import org.cryptodemo.data.CryptoName;
import org.cryptodemo.data.entity.CryptoRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {"datasource.url=jdbc:relique:csv:classpath:test-db"})
class CryptoRepositoryTest {

    @Autowired
    private CryptoRepository repository;

    @Test
    void getCryptoRecordsTimestamps_loadsAndIgnoresInvalidRows() throws SQLException {
        final List<CryptoRecord> records = repository.getCryptoRecordsTimestamps(CryptoName.BTC, 1, 4);
        assertThat(records, contains(
                new CryptoRecord(4, "BTC", new BigDecimal("13")),
                new CryptoRecord(2, "BTC", new BigDecimal("11")),
                new CryptoRecord(1, "BTC", new BigDecimal("10"))
        ));
    }

    @Test
    void getCryptoRecordsTimestamps_noData_throws() {
        assertThrows(SQLException.class,
                () -> repository.getCryptoRecordsTimestamps(CryptoName.XRP, 1, 4));
    }
}