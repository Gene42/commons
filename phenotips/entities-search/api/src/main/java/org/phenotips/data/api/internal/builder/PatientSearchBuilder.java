/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

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

    private void addIdentifierFilter()
    {
        this.newNumberFilter("identifier").setMinValue(0);
    }
}

