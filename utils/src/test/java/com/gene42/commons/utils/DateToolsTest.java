/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils;

import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.gene42.commons.utils.web.WebUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for DateTools.
 *
 * @version $Id$
 */
public class DateToolsTest
{
    @Test
    public void testDateFormat() throws Exception
    {
        String format1 = "yyyy-MM-dd";
        String format2 = "yyyy-M-d";

        String dateStr1 = "2017-01-02";
        String dateStr2 = "2017-1-2";
        Date date1 = DateTools.stringToDate(dateStr1, format1);
        Date date2 = DateTools.stringToDate(dateStr1, format2);

        try {
            DateTools.stringToDate(dateStr2, format1);
            fail();
        } catch (DateTimeParseException e) {
            WebUtils.doNothing(e);
        }



        Date date3 = DateTools.stringToDate(dateStr2, format2);
        assertTrue(DateUtils.truncatedEquals(date1, date3, Calendar.DAY_OF_MONTH));

        assertEquals(dateStr1, DateTools.dateToString(date3, format1));
        assertEquals(dateStr2, DateTools.dateToString(date3, format2));
    }
}
