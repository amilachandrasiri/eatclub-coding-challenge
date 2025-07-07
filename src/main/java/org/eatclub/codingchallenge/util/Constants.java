package org.eatclub.codingchallenge.util;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class Constants {
    public final static DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("h:mm")
        .optionalStart()
        .appendPattern("a")
        .optionalEnd()
        .parseDefaulting(ChronoField.AMPM_OF_DAY, 0)
        .toFormatter(Locale.US);
}

