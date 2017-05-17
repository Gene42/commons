/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal;

import org.phenotips.data.Patient;
import org.phenotips.data.events.PatientChangingEvent;
import org.phenotips.data.internal.PhenoTipsPatient;

import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.context.Execution;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * A handler that listens for updates on a Patient document triggered by changes to data contained inside
 * of a form element identified by {@code JSONFormDataUpdater.FORM_UPDATE_KEY}
 *
 * @version $Id$
 */
@Component
@Named(JSONFormDataUpdater.NAME)
@Singleton
public class JSONFormDataUpdater extends AbstractEventListener
{
    public static final String NAME = "json-form-data-updater";

    private static final String FORM_UPDATE_KEY = "json_form_update";

    @Inject
    private Execution execution;

    @Inject
    private Logger logger;

    /** Needed for getting access to the request. */
    @Inject
    private Container container;

    /**
     * Default constructor, sets up the listener name and the list of events to subscribe to.
     */
    public JSONFormDataUpdater()
    {
        super("json-form-data-updater", new PatientChangingEvent());
    }

    public String getName()
    {
        return this.NAME;
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (this.container.getRequest() == null) {
            return;
        }

        String[] formUpdate = ((ServletRequest) this.container.getRequest()).getHttpServletRequest()
            .getParameterValues(this.FORM_UPDATE_KEY);
        if (formUpdate == null) {
            return;
        }

        Patient patient = new PhenoTipsPatient((XWikiDocument) source);

        for (String jsonString : formUpdate) {
            try {
                JSONObject formUpdateJson = new JSONObject(jsonString);
                patient.updateFromJSON(formUpdateJson);
            } catch (JSONException e) {
                this.logger.warn("Update failed, error parsing form data to JSONObject: [{}]", e.getMessage());
                continue;
            }
        }
    }
}
