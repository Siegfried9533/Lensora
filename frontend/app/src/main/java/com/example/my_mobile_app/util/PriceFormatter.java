package com.example.my_mobile_app.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Formats numeric prices as Vietnamese Dong, e.g. 25000000 -> "₫25.000.000". */
public final class PriceFormatter {

    private static final Locale VI_VN = Locale.forLanguageTag("vi-VN");
    private static final String SYMBOL = "₫";

    private PriceFormatter() {}

    public static String format(double amount) {
        NumberFormat fmt = NumberFormat.getInstance(VI_VN);
        fmt.setMaximumFractionDigits(0);
        return SYMBOL + fmt.format(amount);
    }
}
