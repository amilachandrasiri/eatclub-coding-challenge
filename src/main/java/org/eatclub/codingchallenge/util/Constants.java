package org.eatclub.codingchallenge.util;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Constants {
    public final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mma").localizedBy(Locale.US);
}
