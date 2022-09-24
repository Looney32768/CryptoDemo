package org.cryptodemo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cryptodemo.services.CryptoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;

@SpringBootTest
//@WebMvcTest
@AutoConfigureMockMvc
class AppControllerTest {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getTopCryptos_noPresentData() throws Exception {
        mockMvc.perform(get("/crypto/top"))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("[]"))
                .andExpect(status().isOk());
    }

    @Test
    void getTopCryptos_expectNonEmptyResponse() throws Exception {
        mockMvc.perform(get("/crypto/top")
                        .param("monthsBefore", String.valueOf(getMonthsSinceLastDataTimestamp())))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(containsString("\"normalizedRange\"")))
                .andExpect(status().isOk());
    }

    @Test
    void getTopCryptos_invalidMonthsBefore() throws Exception {
        mockMvc.perform(get("/crypto/top").param("monthsBefore", "-1"))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(containsString("monthsBefore: must be greater than or equal to 0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCryptoPricesInfo_notFound() throws Exception {
        mockMvc.perform(get("/crypto/pricesInfo/NEW_UNSUPPORTED"))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("{\"type\":\"crypto-not-found\",\"message\":\"No data exists for NEW_UNSUPPORTED\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCryptoPricesInfo_empty() throws Exception {
        mockMvc.perform(get("/crypto/pricesInfo/BTC"))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("{\"cryptoName\":\"BTC\",\"earliestTimestamp\":null,\"latestTimestamp\":null,\"priceStats\":null}"))
                .andExpect(status().isOk());
    }

    @Test
    void getCryptoPricesInfo_nonEmpty() throws Exception {
        mockMvc.perform(get("/crypto/pricesInfo/BTC")
                        .param("monthsBefore", String.valueOf(getMonthsSinceLastDataTimestamp())))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(containsString("\"newest\":")))
                .andExpect(status().isOk());
    }

    @Test
    void getCryptoPricesInfo_invalidMonthsBefore() throws Exception {
        mockMvc.perform(get("/crypto/pricesInfo/BTC")
                        .param("monthsBefore", "-1"))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(containsString("monthsBefore: must be greater than or equal to 0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDayTopCrypto_defaultDate_expectNotFound() throws Exception {
        mockMvc.perform(get("/crypto/dayTop"))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(containsString("No data found for any crypto for given date")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDayTopCrypto_stringDate_expectFound() throws Exception {
        mockMvc.perform(get("/crypto/dayTop").param("date", "2022-01-13"))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("{\"crypto\":{\"cryptoName\":\"XRP\",\"earliestTimestamp\":1642039200000,\"latestTimestamp\":1642100400000,\"priceStats\":{\"oldest\":0.7921,\"newest\":0.7686,\"min\":0.7686,\"max\":0.793}},\"normalizedRange\":0.0317}"))
                .andExpect(status().isOk());
    }

    @Test
    void getDayTopCrypto_unixDate_expectFound() throws Exception {
        mockMvc.perform(get("/crypto/dayTop").param("date", "1642881600000"))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("{\"crypto\":{\"cryptoName\":\"DOGE\",\"earliestTimestamp\":1642827600000,\"latestTimestamp\":1642863600000,\"priceStats\":{\"oldest\":0.1433,\"newest\":0.1294,\"min\":0.129,\"max\":0.1433}},\"normalizedRange\":0.1109}"))
                .andExpect(status().isOk());
    }

    @Test
    void getDayTopCrypto_invalidDate() throws Exception {
        mockMvc.perform(get("/crypto/dayTop").param("date", "2022/13/01"))
                .andDo(print())
                .andExpect(header().string("content-type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("{\"type\":\"bad-request\",\"message\":\"Text '2022/13/01' could not be parsed at index 4\"}"))
                .andExpect(status().isBadRequest());
    }

    private static int getMonthsSinceLastDataTimestamp() {
        return Period.between(LocalDate.of(2022, 1, 31), LocalDate.now()).getMonths();
    }
}