/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

import java.util.List;

/**
 * Builder Class for generating a DocumentSearch input JSONObject specifically geared towards PhenoTips Family
 * Documents.
 *
 * @version $Id$
 */
public class FamilySearchBuilder extends DocumentSearchBuilder
{
    /** Class name of a Patient Document. */
    public static final String FAMILY_CLASS = "PhenoTips.FamilyClass";

    /** Property. */
    public static final String MEMBERS_PROPERTY = "members";

    /** Property. */
    public static final String PROBAND_ID_PROPERTY = "proband_id";

    /** Property. */
    public static final String EXTERNAL_ID_PROPERTY = "external_id";

    /**
     * Constructor.
     */
    public FamilySearchBuilder()
    {
        super(FAMILY_CLASS);
        this.addIdentifierFilter();
    }

    /**
     * Add search for a family with the given proband.
     * @param patientId the id of the patient
     * @return this object
     */
    public StringFilterBuilder newProbandFilter(String patientId) {
        return (StringFilterBuilder) this.newStringFilter(PROBAND_ID_PROPERTY).setValue(patientId);
    }

    /**
     * Add filter for families who have any of these patient ids.
     * @param patientIds patient ids to search for
     * @return this object
     */
    public ListFilterBuilder newMembersFilter(List<String> patientIds) {
        return (ListFilterBuilder) this.newListFilter(MEMBERS_PROPERTY).setValues(patientIds);
    }

    /**
     * Add filter for families who have this patient id as a member.
     * @param patientId the patient id to look for
     * @return this object
     */
    public ListFilterBuilder newMembersFilter(String patientId) {
        return (ListFilterBuilder) this.newListFilter(MEMBERS_PROPERTY).setValue(patientId);
    }

    /**
     * Creates a new sub query of type patient focusing on the proband patient.
     * @return the given subQuery
     */
    public PatientSearchBuilder newPatientProbandIdSubQuery() {

        // TODO: the level might need to be computed based on depth of subquery
        return (PatientSearchBuilder) this.newSubQuery(new PatientSearchBuilder())
                                          .newStringFilter("doc.fullName")
                                          .addReferenceValue(new ReferenceValue()
                                              .setLevel(-1)
                                              .setPropertyName(PROBAND_ID_PROPERTY)
                                              .setSpaceAndClass(FAMILY_CLASS)).back()
                                          .newReferenceFilter("reference")
                                          .setSpaceAndClass("PhenoTips.FamilyReferenceClass").back();
    }


    private void addIdentifierFilter()
    {
        this.newNumberFilter("identifier").setMinValue(0);
    }
}

