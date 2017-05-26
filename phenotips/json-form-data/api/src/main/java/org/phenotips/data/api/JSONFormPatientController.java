package org.phenotips.data.api;

import org.phenotips.data.PatientData;
import org.phenotips.data.PatientDataController;

import org.xwiki.component.annotation.Role;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Interface for controllers which can be updated by a form with a JSON parameter value.
 * @param <T>
 * @version $Id$
 */
@Role
public interface JSONFormPatientController<T> extends PatientDataController<T>
{
    /**
     * Saves the PatientData given to the document provided.
     * @param patientData the PatientData to save
     * @param xWikiDocument the document in which to save the data
     */
    void saveForm(PatientData<T> patientData, XWikiDocument xWikiDocument);
}
