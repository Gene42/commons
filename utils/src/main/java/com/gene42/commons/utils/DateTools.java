/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils;

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
    private static final String ZULU_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

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
    }
}
