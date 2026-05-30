package com.example.my_mobile_app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/** Lightweight ISO-8601 parser and dd/MM/yyyy formatter. */
public final class DateUtils {

    private static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String API_DATE_PATTERN = "yyyy-MM-dd";
    private static final String DISPLAY_PATTERN = "dd/MM/yyyy";

    private DateUtils() {}

    public static Date parseIso(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        // Truncate fractional seconds and timezone if present.
        String trimmed = iso;
        int dot = trimmed.indexOf('.');
        if (dot > 0) trimmed = trimmed.substring(0, dot);
        if (trimmed.endsWith("Z")) trimmed = trimmed.substring(0, trimmed.length() - 1);
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_PATTERN, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(trimmed);
        } catch (ParseException e) {
            try {
                return new SimpleDateFormat(API_DATE_PATTERN, Locale.US).parse(trimmed);
            } catch (ParseException ignored) {
                return null;
            }
        }
    }

    public static String formatDisplay(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat(DISPLAY_PATTERN, Locale.US).format(date);
    }

    public static long daysBetween(Date start, Date end) {
        if (start == null || end == null) return 0L;
        long diff = end.getTime() - start.getTime();
        return TimeUnit.MILLISECONDS.toDays(diff);
    }
}
