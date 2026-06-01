package com.example.my_mobile_app.util;

import java.text.Normalizer;

/** Normalizes user-entered Vietnamese text before sending it to the API. */
public final class TextNormalizer {

    private TextNormalizer() {}

    public static String trimAndNormalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFC);
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFC);
    }
}
