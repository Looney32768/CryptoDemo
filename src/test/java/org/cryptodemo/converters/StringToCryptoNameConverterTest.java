package org.cryptodemo.converters;

import org.cryptodemo.data.CryptoName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringToCryptoNameConverterTest {

    private StringToCryptoNameConverter converter = new StringToCryptoNameConverter();

    @Test
    public void convert_upperCase() {
        assertEquals(CryptoName.BTC, converter.convert("BTC"));
    }

    @Test
    public void convert_anyCase() {
        assertEquals(CryptoName.BTC, converter.convert("bTc"));
    }

    @Test()
    public void convert_invalid() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> converter.convert("unknown"));
    }
}