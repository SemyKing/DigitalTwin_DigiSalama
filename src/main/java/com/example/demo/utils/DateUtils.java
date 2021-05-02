package com.example.demo.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private final static DateTimeFormatter databaseFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private final static DateTimeFormatter defaultFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static DateTimeFormatter getDatabaseFormat() {
        return databaseFormat;
    }
    public static DateTimeFormatter getDefaultFormat() {
        return defaultFormat;
    }

    public static LocalDateTime stringToLocalDateTime(String dateString) throws Exception {
        if (dateString == null) {
            return null;
        }

        if (dateString.length() < 16) {
            throw new Exception("Date: '" + dateString + "' is invalid, required date format: 'yyyy-MM-ddTHH:mm'");
        }
        return LocalDateTime.parse(dateString.substring(0, 16), databaseFormat);
    }
}
