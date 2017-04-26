/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal;

import org.xwiki.text.StringUtils;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class SearchUtilsTest
{
    /**
     * Class set up.
     *
     * @throws  Exception  on error
     */
    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    /**
     * Class tear down.
     *
     * @throws  Exception  on error
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test set up.
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test tear down.
     */
    @After
    public void tearDown() {
    }


    @Test
    public void getJSONArrayTest() throws Exception
    {
        String key = "key";
        JSONObject obj = new JSONObject();
        JSONArray arr = SearchUtils.getJSONArray(obj, key);
        assertNotNull(arr);
    }

    @Test
    public void getFirstStringTest() throws Exception
    {
        assertEquals(StringUtils.EMPTY, SearchUtils.getFirstString(null));
        assertEquals(StringUtils.EMPTY, SearchUtils.getFirstString(StringUtils.EMPTY));
        assertEquals("test", SearchUtils.getFirstString(new Object [] {"test", "blah"}));
        assertEquals("7", SearchUtils.getFirstString(Arrays.asList(7, "blah", new JSONObject())));
    }
}
