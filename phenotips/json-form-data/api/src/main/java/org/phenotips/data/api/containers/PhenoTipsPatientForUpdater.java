package org.phenotips.data.api.containers;

import org.phenotips.data.PatientData;
import org.phenotips.data.internal.PhenoTipsPatient;

import java.util.Map;
import java.util.TreeMap;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Extended PhenoTipsPatient which overrides the getData method. It is needed to circumvent the private member
 * extraData.
 *
 * @version $Id$
 */
public class PhenoTipsPatientForUpdater extends PhenoTipsPatient
{
    /** Extra data that can be plugged into the patient record. */
    private Map<String, PatientData<?>> extraData = new TreeMap<>();

    /**
     * Constructor.
     * @param doc the XWikiDocument of this patient
     */
    public PhenoTipsPatientForUpdater(XWikiDocument doc)
    {
        super(doc);
    }

    /**
     * Adds the given PatientData to the patient.
     * @param patientData the data to add
     */
    public void addData(PatientData<?> patientData)
    {
        this.extraData.put(patientData.getName(), patientData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> PatientData<T> getData(String name)
    {
        PatientData<T> data = (PatientData<T>) this.extraData.get(name);

        if (data == null) {
            return super.getData(name);
        } else {
            return data;
        }
    }
}
