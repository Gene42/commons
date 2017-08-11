/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal;

import org.phenotips.data.api.internal.builder.DocumentSearchBuilder;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class DocumentSearchBuilderTest
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
    public void test1() throws Exception
    {
        DocumentSearchBuilder query = new DocumentSearchBuilder("PhenoTips.PatientClass");

        String value = "haha";

        query.newNumberFilter("identifier").setMinValue(0);
        query.newObjectFilter().setSpaceAndClass("PhenoTips.SourceFileClass").back()
            .newExpression().setJoinModeToOr()
            .newStringFilter("first_name").setValue(value).back()
            .newStringFilter("last_name").setValue(value).back()
            .newStringFilter("fileName").setValue(value).setSpaceAndClass("PhenoTips.SourceFileClass").back()
            .back()
            .onlyForUser("xwiki:Xwiki.Bob", Arrays.asList("xwiki:Groups.Administrators"))
             .newSubQuery("TestClass").back()
        .setSortOrder("first_name", "desc");

        System.out.println(query.build().toString(4));
    }
}
