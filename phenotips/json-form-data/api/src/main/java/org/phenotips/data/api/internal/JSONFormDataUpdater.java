/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal;

import org.phenotips.data.internal.PhenoTipsPatient;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.event.ActionExecutingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletRequest;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.gene42.commons.controllers.locks.LockWithTimeout;
import com.gene42.commons.controllers.locks.PatientSaveLock;
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

    @Inject
    @Named(PatientSaveLock.NAME)
    private LockWithTimeout lockWithTimeout;

    /** Needed for getting access to the request. */
    @Inject
    private Container container;

    @Inject
    private DocumentAccessBridge documentAccessBridge;

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
        this.logger.error("ActionExecutingEvent: save | " + event.toString());

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

        String patientId = doc.getDocumentReference().getName();
        try {
            if (!this.lockWithTimeout.acquireLockOrReturn(patientId, 60, TimeUnit.SECONDS, 2)) {
                this.logger.error("ActionExecutingEvent: save | failed to acquire lock, will not save");
                throw new ConcurrentModificationException(
                    String.format("Patient [%s] is already being saved. Refresh data and try again.", patientId));
            }

            doc = (XWikiDocument) this.documentAccessBridge.getDocument(doc.getDocumentReference());
            // 1. Aggregate all form JSONs into one, so that we don't have to save the document more than once
            for (String jsonString : formUpdate) {
                aggregate(aggregate, jsonString);
            }
            this.logger.error("ActionExecutingEvent: save | pre-update");
            // 2. Call all controllers and save the document
            new PhenoTipsPatient(doc).updateFromJSON(aggregate);
            this.logger.error("ActionExecutingEvent: save | post-update");
        } catch (JSONException e) {
            this.logger.warn(
                String.format("Update failed, error parsing form data to JSONObject: [%s]", e.getMessage()), e);
        } catch (Exception e) {
            this.logger.debug(String.format("Unable to update patient from JSON [%s]", aggregate), e);
        } /*finally {
            this.lockWithTimeout.releaseLock(patientId);
        }  */
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
