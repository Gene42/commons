/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Optional;

/**
 * Utilities class for handling dates.
 *
 * @version $Id$
 */
public final class DateTools
{
    /** Zulu time string format. */
    public static final String ZULU_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /** The UTC ZoneId. */
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private static final DateTimeFormatter ZULU_FORMATTER = getDateFormatter(ZULU_TIME_FORMAT);

    private DateTools()
    {
    }

    /**
     * Gets the date format specified by the string input.
     *
     * @param dateFormat      the string value of date format
     * @return                the date format
     */
    public static DateTimeFormatter getDateFormatter(String dateFormat)
    {
        return new DateTimeFormatterBuilder().appendPattern(dateFormat)
                                      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                                      .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                                      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                                      .toFormatter().withZone(UTC_ZONE);
    }

    /**
     * Returns a DateTimeFormatter for zulu time.
     * @return  the date format
     */
    public static DateTimeFormatter getZuluDateFormatter()
    {
        return ZULU_FORMATTER;
    }

    /**
     * Parses the given string with the given format, to create a Date object. The UTC Zone is used.
     * @param value the string value to interpret as a date
     * @param format the formatting pattern of the string to use during parsing
     * @return a Date object
     */
    public static Date stringToDate(String value, String format)
    {
        return stringToDate(value, getDateFormatter(format));
    }

    /**
     * Parses the given string with the given formatter, to create a Date object.
     * @param value the string value to interpret as a date
     * @param formatter the formatter to use for parsing
     * @return a Date object
     */
    public static Date stringToDate(String value, DateTimeFormatter formatter)
    {
        TemporalAccessor dt = getDateFormatter(formatter)
            .parseBest(value, LocalDateTime::from, LocalDate::from, LocalTime::from, YearMonth::from);

        LocalDateTime dateTime;

        if (dt instanceof LocalDate) {
            dateTime = ((LocalDate) dt).atStartOfDay();
        } else if (dt instanceof LocalTime) {
            dateTime = ((LocalTime) dt).atDate(LocalDate.now());
        } else if (dt instanceof YearMonth) {
            dateTime = ((YearMonth) dt).atDay(1).atStartOfDay();
        } else {
            dateTime = LocalDateTime.from(dt);
        }

        return Date.from(dateTime.toInstant(ZoneOffset.UTC));
    }


    /**
     * Parses the given string with the given formatter (in UTC) to get the milliseconds from the epoch
     * of 1970-01-01T00:00:00Z.
     * @param value the string value to parse
     * @param formatter the formatter to use for parsing. If null, the ZULU formatter is used
     * @return the resulting millisecond value
     */
    public static long stringToMillis(String value, DateTimeFormatter formatter)
    {
        if (formatter == null) {
            return LocalDateTime.parse(value, ZULU_FORMATTER).toInstant(ZoneOffset.UTC).toEpochMilli();
        } else {
            return LocalDateTime.parse(value, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
        }
    }

    /**
     * Converts the given the milliseconds from the epoch of 1970-01-01T00:00:00Z, into a string given the
     * formatter. The UTC Zone is used.
     * @param value the milliseconds value to use
     * @param formatter the formatter to use. If null, the ZULU formatter is used
     * @return a String
     */
    public static String millisToString(long value, DateTimeFormatter formatter)
    {
        return localDateTimeToString(millisToLocalDateTime(value), formatter);
    }

    /**
     * Converts the given date in a String using the given format pattern. The UTC Zone is used.
     * @param date the Date to convert
     * @param format the formatting pattern
     * @return a String
     */
    public static String dateToString(Date date, String format)
    {
        return dateToString(date, getDateFormatter(format));
    }

    /**
     * Converts the given date in a String using the given formatter. If the formatter is null,
     * a Zulu time formatter is used (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
     * @param date the Date to convert
     * @param formatter the formatter to use
     * @return a String
     */
    public static String dateToString(Date date, DateTimeFormatter formatter)
    {
        return getDateFormatter(formatter).format(date.toInstant());
    }

    private static DateTimeFormatter getDateFormatter(DateTimeFormatter formatter)
    {
        return Optional.ofNullable(formatter).orElse(ZULU_FORMATTER).withResolverStyle(ResolverStyle.LENIENT);
    /**
     * Converts the given Date into a LocalDateTime (UTC)
     * @param date the Date to convert
     * @return the resulting LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(UTC_ZONE).toLocalDateTime();
    }

    /**
     * Converts milliseconds from the epoch of 1970-01-01T00:00:00Z to a LocalDateTime (UTC)
     * @param milliseconds milliseconds to convert
     * @return the resulting LocalDateTime
     */
    public static LocalDateTime millisToLocalDateTime(long milliseconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), UTC_ZONE);
    }

    /**
     * Converts given LocalDateTime (UTC) to milliseconds from the epoch of 1970-01-01T00:00:00Z
     * @param localDateTime LocalDateTime to convert
     * @return the resulting millisecond value
     */
    public static long localDateTimeToMillis(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * Converts the given LocalDateTime into a String using the given formatter. The UTC Zone is used.
     * @param localDateTime the LocalDateTime to convert
     * @param formatter the formatter to use. If null, the ZULU formatter is ued
     * @return a String
     */
    public static String localDateTimeToString(LocalDateTime localDateTime, DateTimeFormatter formatter) {
        if (formatter == null) {
            return ZULU_FORMATTER.format(localDateTime.toInstant(ZoneOffset.UTC));
        } else {
            return formatter.format(localDateTime.toInstant(ZoneOffset.UTC));
        }
    }
}
