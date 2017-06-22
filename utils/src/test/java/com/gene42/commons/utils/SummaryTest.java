/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for Summary.
 *
 * @version $Id$
 */
public class SummaryTest
{

    private static final String SKIP_LINE = "Skipped line";

    private static final String COLUMN1 = "Make";

    private static final String COLUMN2 = "Color";

    private static final Logger LOGGER = LoggerFactory.getLogger(Summary.class);

    @Test
    public void checkRow() throws Exception
    {
        Summary variants = new Summary("Validation").sortedByKey().setMaxMessagesKept(20);
        Summary columns = new Summary("Parsing").insertionOrdered().setMaxMessagesKept(200);


        variants.error(SKIP_LINE);
        variants.error(SKIP_LINE);
        variants.error(SKIP_LINE);

        columns.warn(COLUMN1);
        columns.warn(COLUMN2);
        columns.warn(COLUMN2);

        variants.merge(columns);

        Summary finalSummary = new Summary().insertionOrdered().setLogStringVariable("{}");
        finalSummary.info("Number of variants parsed", 2);
        finalSummary.merge(variants);
        finalSummary.log(LOGGER);
    }
}
