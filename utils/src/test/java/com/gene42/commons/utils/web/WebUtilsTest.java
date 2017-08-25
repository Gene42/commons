/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.web;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test class for JsonApiErrorBuilder.
 *
 * @version $Id$
 */
public class WebUtilsTest
{
    @Test
    public void getErrorResponseTest() throws Exception
    {
        Response response = WebUtils.getErrorResponse(
            Response.Status.BAD_REQUEST,
            "Source File Name can only have .vcf extension", "/blah/blah2/data", "data");

        assertEquals(400, response.getStatus());
    }

    @Test
    public void getJSONObjectValueTest() throws Exception
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("String", "stringValue");
        jsonObject.put("JSONObject", new JSONObject());
        jsonObject.put("JSONArray", new JSONArray());

        assertNotNull(WebUtils.getJSONObjectValue(jsonObject, "JSONObject", JSONObject.class));

        try {
            WebUtils.getJSONObjectValue(jsonObject, "Double", Double.class);
            fail();
        } catch (WebApplicationException e) {
            System.out.println(e.getResponse().getEntity().toString());
            // Pass
        }
    }

    @Test
    public void castJSONObjectTest() throws Exception
    {
        try {
            WebUtils.castJSONObject(new JSONArray(), JSONObject.class);
            fail();
        } catch (WebApplicationException e) {
            System.out.println(e.getResponse().getEntity().toString());
            // Pass
        }
    }

}
