package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.internal.filter.AbstractFilter;
import org.phenotips.data.api.internal.filter.ObjectFilter;

import org.json.JSONObject;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class ObjectFilterBuilder extends AbstractFilterBuilder<String>
{
    /**
     * Constructor.
     * @param parent the parent query builder
     */
    public ObjectFilterBuilder(DocumentSearchBuilder parent)
    {
        super(ObjectFilter.DEFAULT_PROPERTY_NAME, parent);
    }


    @Override
    public JSONObject build()
    {
        JSONObject filter = super.build();
        filter.put(AbstractFilter.TYPE_KEY, ObjectFilter.TYPE);
        return filter;
    }
}
