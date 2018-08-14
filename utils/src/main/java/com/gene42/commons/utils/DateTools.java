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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Utilities class for handling dates.
 *
 * @version $Id$
 */
public final class DateTools
{
    private static final String ZULU_TIME_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'";

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
        return DateTimeFormatter.ofPattern(dateFormat).withZone(UTC_ZONE);
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
        return stringToDate(value, DateTimeFormatter.ofPattern(format).withZone(UTC_ZONE));
    }

    /**
     * Parses the given string with the given formatter, to create a Date object.
     * @param value the string value to interpret as a date
     * @param formatter the formatter to use for parsing
     * @return a Date object
     */
    public static Date stringToDate(String value, DateTimeFormatter formatter)
    {
        return Date.from(LocalDate.parse(value, formatter).atStartOfDay(formatter.getZone()).toInstant());
    }

    /**
     * Converts the given date in a String using the given format pattern. The UTC Zone is used.
     * @param date the Date to convert
     * @param format the formatting pattern
     * @return a String
     */
    public static String dateToString(Date date, String format)
    {
        return dateToString(date, DateTimeFormatter.ofPattern(format).withZone(UTC_ZONE));
    }

    /**
     * Converts the given date in a String using the given formatter. If the formatter is null,
     * a Zulu time formatter is used (yyyy-MM-dd'T'hh:mm:ss.SSS'Z')
     * @param date the Date to convert
     * @param formatter the formatter to use
     * @return a String
     */
    public static String dateToString(Date date, DateTimeFormatter formatter)
    {
        if (formatter == null) {
            return ZULU_FORMATTER.format(date.toInstant());
        } else {
            return formatter.format(date.toInstant());
        }
    }

    /**
     * Converts the given Date into a LocalDateTime
     * @param date the Date to convert
     * @return the resulting LocalDateTime
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(UTC_ZONE).toLocalDateTime();
    }
}
