package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.internal.filter.StringFilter;

import org.json.JSONObject;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class StringFilterBuilder extends AbstractFilterBuilder<String>
{
    private String match;

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

    @Override
    public JSONObject build()
    {
        JSONObject filter = super.build();

        if (this.match != null) {
            filter.put(StringFilter.MATCH_KEY, this.match);
        }

        return filter;
    }
}
