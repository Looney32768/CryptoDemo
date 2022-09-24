package org.cryptodemo.dal;

import org.cryptodemo.data.CryptoName;
import org.cryptodemo.data.entity.CryptoRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class CryptoRepository implements InitializingBean {

    @Value("${datasource.url}")
    private String datasourceUrl;
    private Connection connection; // todo: replace with managed pool

    /**
     * Fetch data for given crypto from DB (local file).
     * @param cryptoName to get statistics for
     * @param fromTimestamp specifies the earliest point in time we are interested in
     * @param untilTimestamp specifies the latest point in time we are interested in
     * @return results sorted by timestamp in descending order
     * @throws SQLException on DB error
     */
    public List<CryptoRecord> getCryptoRecordsTimestamps(final CryptoName cryptoName, final long fromTimestamp, final long untilTimestamp) throws SQLException {
        final String tableName = cryptoName.name() + "_values";
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tableName+ " t WHERE t.timestamp BETWEEN ? AND ? ORDER BY t.timestamp DESC")) {
            statement.setLong(1, fromTimestamp);
            statement.setLong(2, untilTimestamp);
            final ResultSet results = statement.executeQuery();
            final List<CryptoRecord> cryptoRecords = new ArrayList<>();
            while (results.next()) {
                cryptoRecords.add(new CryptoRecord(results.getLong(1), results.getString(2), new BigDecimal(results.getString(3))));
            }
            return cryptoRecords;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Properties props = new Properties();
        // Define column names and column data types here.
        props.put("suppressHeaders", "false");
        props.put("headerline", "timestamp,symbol,price");
        props.put("columnTypes", "Long,String,String");
        props.put("ignoreNonParseableLines", "true");
        connection = DriverManager.getConnection(datasourceUrl, props);
    }
}
