package org.cryptodemo.converters;

import org.cryptodemo.data.CryptoName;
import org.springframework.core.convert.converter.Converter;

public class StringToCryptoNameConverter implements Converter<String, CryptoName> {

    @Override
    public CryptoName convert(String source) {
        return CryptoName.valueOf(source.toUpperCase());
    }
}