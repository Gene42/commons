/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.web;

import javax.ws.rs.core.Response;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

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
}
