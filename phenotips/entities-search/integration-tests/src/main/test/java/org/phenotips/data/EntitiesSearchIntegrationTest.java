package org.phenotips.data;

import com.gene42.commons.utils.web.HttpEndpoint;

import org.phenotips.data.api.internal.builder.PatientSearchBuilder;
import org.phenotips.data.rest.internal.EntitySearchRequestBuilder;
import org.phenotips.data.rest.internal.EntitySearchRestEndpoint;

import org.junit.Test;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class EntitiesSearchIntegrationTest {

    @Test
    public void qwgadsf() throws Exception
    {
        try (EntitySearchRestEndpoint endpoint =
                 new EntitySearchRestEndpoint(HttpEndpoint.builder().setHost("local.phenotips.com")
                                                          .setUsernameAndPassword("Admin", "admin")
                                                          .build())) {

            endpoint.search(new EntitySearchRequestBuilder()
                .setDocumentSearchBuilder(new PatientSearchBuilder()))
                    .getItems()
                    .forEach(j -> System.out.println(j.toString(4)));
        }
    }

}
