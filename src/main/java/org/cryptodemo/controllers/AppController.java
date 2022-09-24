package org.cryptodemo.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.cryptodemo.data.CryptoName;
import org.cryptodemo.data.dto.CryptoStatsWithNormalizedRange;
import org.cryptodemo.data.dto.CryptoTimeRangeStats;
import org.cryptodemo.errors.ApiError;
import org.cryptodemo.errors.DataNotFoundException;
import org.cryptodemo.services.CryptoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.cryptodemo.time.TimeUtils.getUnixEpochMillisNMonthsBack;

@RestController
@RequestMapping("/crypto")
@Validated
public class AppController {

    private final CryptoService cryptoService;

    public AppController(final CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Operation(summary = "Return a descending sorted list of all the cryptos, comparing the normalized range (i.e. (max-min)/min)")
    @Parameters({
            @Parameter(name = "monthsBefore", description = "Number of months to look into past", example = "8")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page of top cryptos",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema( schema = @Schema(implementation = CryptoStatsWithNormalizedRange.class))) }),
    })
    @GetMapping(value = "/top", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @Validated
    public List<CryptoStatsWithNormalizedRange> getTopCryptos(@RequestParam(defaultValue = "0") @Min(0) final int monthsBefore) {
        final long unixEpochMillisFrom = getUnixEpochMillisNMonthsBack(monthsBefore + 1);
        final long unixEpochMillisUntil = getUnixEpochMillisNMonthsBack(monthsBefore);
        return cryptoService.getTopCryptos(unixEpochMillisFrom, unixEpochMillisUntil);
    }

    @Operation(summary = "Return the oldest/newest/min/max values for a requested crypto")
    @Parameters({
            @Parameter(name = "cryptoName", description = "Crypto for which to get prices", example = "ETH"),
            @Parameter(name = "monthsBefore", description = "Number of months to look into past", example = "8")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Last month statistics for requested crypto",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CryptoTimeRangeStats.class)) }),
            @ApiResponse(responseCode = "404", description = "No data available for requested   crypto",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiError.class))}) })
    @GetMapping(value = "/pricesInfo/{cryptoName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @Validated
    public ResponseEntity<?> getCryptoPricesInfo(@PathVariable("cryptoName") final CryptoName cryptoName,
                                                 @RequestParam(defaultValue = "0") @Min(0) final int monthsBefore) {
        try {
            return ResponseEntity.ok(cryptoService.getCryptoInfo(cryptoName, monthsBefore));
        } catch (DataNotFoundException e) {
            return new ResponseEntity<>(new ApiError("crypto-not-found", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Return the crypto with the highest normalized range for a specific day")
    @Parameters({
            @Parameter(name = "date", description = "Date for which to find top crypto",
                    examples = {@ExampleObject(name="ISO date", value = "2022-01-13"),
                            @ExampleObject(name="Unix timestamp", value = "1642881600000")})
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top valued crypto for given date",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CryptoStatsWithNormalizedRange.class)) }),
            @ApiResponse(responseCode = "404", description = "No data exists for specified date",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiError.class)) }),
    })
    @GetMapping(value = "/dayTop", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getDayTopCrypto(
            @RequestParam(value = "date",
                    defaultValue = "#{T(java.time.LocalDate).now().format(T(java.time.format.DateTimeFormatter).ISO_LOCAL_DATE)}") final String date) {
        LocalDate localDate;
        try {
            localDate = LocalDate.ofInstant(Instant.ofEpochMilli(Long.parseLong(date)), ZoneId.systemDefault());
        } catch (IllegalArgumentException e) {
            localDate = LocalDate.parse(date);
        }
        return cryptoService.getTopCrypto(localDate).<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(new ApiError("data-not-found", "No data found for any crypto for given date: " + localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)), HttpStatus.NOT_FOUND));
    }
}
