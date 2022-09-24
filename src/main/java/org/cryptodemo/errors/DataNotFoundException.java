package org.cryptodemo.errors;

public class DataNotFoundException extends RuntimeException {

    public DataNotFoundException(final String message) {
        super(message);
    }
}
