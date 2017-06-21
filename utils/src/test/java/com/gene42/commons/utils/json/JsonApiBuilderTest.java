/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test class for JsonApiBuilder.
 *
 * @version $Id$
 */
public class JsonApiBuilderTest
{
    @Test
    public void testBuild() throws Exception {

        JsonApiResourceBuilder data1 = new JsonApiResourceBuilder("data1", "type1");
        data1.putAttribute("attr1", "attrValue1").putAttribute("attr2", "attrValue2");
        data1.putParentRelationship("data1", "type1").putSelfLink("/patients/parent1");
        data1.putRelationship("job", "jobId1", "job");
        data1.putLink("job", "/rest/jobs/job1", null);

        JsonApiResourceBuilder data2 = new JsonApiResourceBuilder("data2", "type1").putSelfLink("/include1/stuff2");
        data2.putParentRelationship("data1", "type1");

        JsonApiResourceBuilder include1 = new JsonApiResourceBuilder("parent1", "patient").putSelfLink("/patients/parent1");
        JsonApiResourceBuilder include2 = new JsonApiResourceBuilder("jobId1", "job").putSelfLink("/rest/jobs/job1");

        JSONObject meta = new JSONObject();
        meta.put("meta2", "metaValueX");
        meta.put("meta4", "metaValue4");

        JSONObject result =
        new JsonApiBuilder()
            .putMeta(meta).putMeta("meta1", "metaValue1").putMeta("meta2", "metaValue2")
            .addData(data1).addData(data2)
            .addIncluded(include1).addIncluded(include2)
            .build();

        System.out.println(result.toString(4));

        assertEquals("metaValue2", ((JSONObject)result.get(JsonApiBuilder.META_FIELD)).get("meta2"));
    }

    @Test
    public void testNullDataAndInclude() throws Exception {
        JSONObject result = new JsonApiBuilder()
            .addData(new JsonApiResourceBuilder("data1", "type1"))
            .addData(null).addData(null)
            .addIncluded(null).addIncluded(null).build();
        System.out.println(result.toString(4));
        assertEquals(1, ((JSONArray)result.get(JsonApiResourceBuilder.DATA_FIELD)).length());
        assertFalse(result.has(JsonApiBuilder.INCLUDED_FIELD));
    }
}
