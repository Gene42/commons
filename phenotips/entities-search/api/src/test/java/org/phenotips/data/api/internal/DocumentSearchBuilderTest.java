package org.phenotips.data.api.internal;

import org.phenotips.data.api.internal.builder.DocumentSearchBuilder;
import org.phenotips.data.api.internal.filter.StringFilter;

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
        String match = StringFilter.MATCH_SUBSTRING;

        query.newNumberFilter().set("identifier").setMinValue(0);
        query.newStringFilter().set("visibility", "PhenoTips.VisibilityClass")
            .setValues(Arrays.asList("private", "public", "open"));

        query.newExpression().setJoinModeToOr()
            .newStringFilter().setMatch(match).set("first_name", value).back()
            .newStringFilter().setMatch(match).set("last_name", value).back()
            .newStringFilter().setMatch(match).set("fileName", "PhenoTips.SourceFileClass", value).back()
            .back();

        System.out.println(query.build().toString(4));
    }
}
