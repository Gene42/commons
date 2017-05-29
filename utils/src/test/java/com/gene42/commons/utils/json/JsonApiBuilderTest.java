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

        JsonApiResourceBuilder data2 = new JsonApiResourceBuilder("data2", "type1");
        JsonApiResourceBuilder include1 = new JsonApiResourceBuilder("include1", "type2");
        include1.putParentRelationship("data1", "type1").putSelfLink("/include1/stuff");

        JSONObject meta = new JSONObject();
        meta.put("meta2", "metaValueX");
        meta.put("meta4", "metaValue4");

        JSONObject result =
        new JsonApiBuilder()
            .putMeta(meta).putMeta("meta1", "metaValue1").putMeta("meta2", "metaValue2")
            .addData(data1).addData(data2)
            .addIncluded(include1).build();

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
