package org.cryptodemo.controllers;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.cryptodemo.errors.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class CommonAdvice {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(responseCode = "500", description = "Internal server error",
            content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiError.class)) })
    public ResponseEntity<ApiError> handleRuntimeException(final RuntimeException ex) {
        return ResponseEntity.internalServerError().body(new ApiError("internal-server-error", ex.getMessage()));
    }

    @ExceptionHandler(value = {HttpClientErrorException.BadRequest.class, ConstraintViolationException.class, DateTimeParseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", description = "Bad request",
            content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE) })
    public ResponseEntity<ApiError> handleBadRequestException(final Exception ex) {
        return ResponseEntity.badRequest().body(new ApiError("bad-request", ex.getMessage()));
    }
}
