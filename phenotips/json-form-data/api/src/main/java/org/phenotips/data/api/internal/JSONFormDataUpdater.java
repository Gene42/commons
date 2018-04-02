/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal;

import org.phenotips.data.internal.PhenoTipsPatient;

import org.xwiki.bridge.event.ActionExecutingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * A handler that listens for updates on a Patient document triggered by changes to data contained inside
 * of a form element identified by {@code JSONFormDataUpdater.FORM_UPDATE_KEY}.
 *
 * @version $Id$
 */
@Component
@Named(JSONFormDataUpdater.NAME)
@Singleton
public class JSONFormDataUpdater extends AbstractEventListener
{
    /** Component name. */
    public static final String NAME = "json-form-data-updater";

    private static final String FORM_UPDATE_KEY = "json_form_update";

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
        super(NAME, new ActionExecutingEvent("save"));
    }

    /**
     * Getter for the name.
     * @return the name
     */
    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument doc = (XWikiDocument) source;
        if (this.container.getRequest() == null) {
            return;
        }

        String[] formUpdate = ((ServletRequest) this.container.getRequest()).getHttpServletRequest()
            .getParameterValues(FORM_UPDATE_KEY);
        if (formUpdate == null || formUpdate.length == 0) {
            return;
        }

        JSONObject aggregate = new JSONObject();

        try {
            // 1. Aggregate all form JSONs into one, so that we don't have to save the document more than once
            for (String jsonString : formUpdate) {
                aggregate(aggregate, jsonString);
            }

            // 2. Call all controllers
            new PhenoTipsPatient(doc).updateFromJSON(aggregate, true);

        } catch (JSONException e) {
            this.logger.warn(
                String.format("Update failed, error parsing form data to JSONObject: [%s]", e.getMessage()), e);
        } catch (Exception e) {
            this.logger.debug(String.format("Unable to update patient from JSON [%s]", aggregate), e);
        }
    }

    private static void aggregate(JSONObject aggregate, String formString)
    {
        if (StringUtils.isBlank(formString)) {
            return;
        }

        JSONObject formJson = new JSONObject(formString);

        if (CollectionUtils.isEmpty(formJson.keySet())) {
            return;
        }

        String sectionName = formJson.keys().next();

        aggregate.putOpt(sectionName, formJson.opt(sectionName));
    }
}
