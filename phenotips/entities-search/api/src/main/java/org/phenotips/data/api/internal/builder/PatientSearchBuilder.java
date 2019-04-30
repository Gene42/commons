/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.internal.filter.StringFilter;

/**
 * Builder Class for generating a DocumentSearch input JSONObject specifically geared towards PhenoTips Patient
 * Documents.
 *
 * @version $Id$
 */
public class PatientSearchBuilder extends DocumentSearchBuilder
{
    /** Class name of a Patient Document. */
    public static final String PATIENT_CLASS = "PhenoTips.PatientClass";

    /**
     * Constructor.
     */
    public PatientSearchBuilder()
    {
        super(PATIENT_CLASS);
        this.addIdentifierFilter();
    }

    /**
     * Creates + adds a new string filter builder for the "phenotype" property and returns it.
     * @param phenotype the phenotype value
     * @return a new StringFilterBuilder
     */
    public StringFilterBuilder newPhenotypeFilter(String phenotype)
    {
        return (StringFilterBuilder) this.newStringFilter("phenotype")
        .setMatch(StringFilter.MATCH_CASE_INSENSITIVE)
        .setValue(phenotype);
    }

    private void addIdentifierFilter()
    {
        this.newNumberFilter("identifier").setMinValue(0);
    }
}

