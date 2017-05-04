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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * A handler that listens for updates to the
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
    private static final String FORM_DATA_KEY = "form_data";

    @Inject
    private Execution execution;

    @Inject
    private Logger logger;

    /** Needed for getting access to the request. */
    @Inject
    private Container container;

    public String getName()
    {
        return this.NAME;
    }

    /** Default constructor, sets up the listener name and the list of events to subscribe to. */
    public JSONFormDataUpdater()
    {
        super(this.NAME, new PatientChangingEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (this.container.getRequest() == null) {
            return;
        }

        Patient patient = new PhenoTipsPatient((XWikiDocument) source);

        String formUpdate = ((ServletRequest) this.container.getRequest()).getHttpServletRequest()
            .getParameter(this.FORM_UPDATE_KEY);
        if (formUpdate == null) {
            return;
        }

        JSONObject formUpdateJson;
        JSONArray formData;
        try {
            formUpdateJson = new JSONObject(formUpdate);
            formData = formUpdateJson.getJSONArray(this.FORM_DATA_KEY);
        } catch (JSONException e) {
            this.logger.warn("Failed to parse form data to JSON: [{}]", e.getMessage());
            return;
        }

        JSONObject jsonObject;
        try {
            for (int i = 0; i < formData.length(); i++) {
                jsonObject = formData.getJSONObject(i);
                patient.updateFromJSON(jsonObject);
            }
        } catch (JSONException e) {
            this.logger.warn("Failed to retrieve JSON object from JSON form data update: [{}]", e.getMessage());
            return;
        }
    }
}
