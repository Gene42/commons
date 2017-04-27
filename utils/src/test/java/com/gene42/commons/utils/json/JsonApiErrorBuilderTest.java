package com.gene42.commons.utils.json;

import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.junit.Test;

import com.gene42.commons.utils.web.WebUtils;

/**
 * Test class for JsonApiErrorBuilder.
 *
 * @version $Id$
 */
public class JsonApiErrorBuilderTest
{
    @Test
    public void testBuild() throws Exception
    {

        JsonApiErrorBuilder builder = new JsonApiErrorBuilder();
        builder
            .setId("1e009cce-2b7b-11e7-93ae-92361f002671")
            .setStatus("400")
            .setDetail("Source File Name can only have .vcf extension")
            .setAboutLink("/rest/errors/1e009cce-2b7b-11e7-93ae-92361f002671/about").setCode("171")
            .setSourcePointer("/rest/variant-source-files/patients/P0000007/files/file1.png")
            .setSourceParameter("file1.png")
            .setTitle("Bad Request");

        JSONObject result = builder.build();

        System.out.println(result.toString(4));
    }

    @Test
    public void web() throws Exception
    {
        System.out.println(WebUtils.getErrorResponse(
            Response.Status.BAD_REQUEST,
            "Source File Name can only have .vcf extension", "/blah/blah2/data", "data"));
    }
}
