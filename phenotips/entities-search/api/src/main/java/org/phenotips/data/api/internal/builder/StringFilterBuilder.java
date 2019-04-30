/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.internal.PropertyName;
import org.phenotips.data.api.internal.filter.StringFilter;

import org.json.JSONObject;

/**
 * Builder class for a StringFilter.
 *
 * @version $Id$
 */
public class StringFilterBuilder extends AbstractFilterBuilder<String>
{
    private String match;
    private Boolean subTerms;

    /**
     * Constructor.
     * @param propertyName the name of the property
     * @param parent the parent query builder
     */
    public StringFilterBuilder(String propertyName, DocumentSearchBuilder parent)
    {
        super(propertyName, parent);
    }

    /**
     * Getter for match.
     *
     * @return match
     */
    public String getMatch()
    {
        return this.match;
    }

    /**
     * Setter for match.
     *
     * @param match match to set
     * @return this object
     */
    public StringFilterBuilder setMatch(String match)
    {
        this.match = match;
        return this;
    }

    /**
     * Getter for subTerms.
     *
     * @return subTerms
     */
    public boolean isSubTerms() {
        return Boolean.TRUE.equals(this.subTerms);
    }

    /**
     * Setter for subTerms.
     *
     * @param subTerms subTerms to set
     * @return this object
     */
    public StringFilterBuilder setSubTerms(Boolean subTerms) {
        this.subTerms = subTerms;
        return this;
    }

    @Override
    public JSONObject build()
    {
        JSONObject filter = super.build();

        if (this.match != null) {
            filter.put(StringFilter.MATCH_KEY, this.match);
        }

        if (this.subTerms != null) {
            filter.put(PropertyName.SUBTERMS_KEY, this.subTerms.toString());
        }

        return filter;
    }
}
