package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.internal.filter.NumberFilter;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class NumberFilterBuilder extends AbstractFilterBuilder<Number>
{
    /**
     * Constructor.
     * @param propertyName the name of the property
     * @param parent the parent query builder
     */
    public NumberFilterBuilder(String propertyName, DocumentSearchBuilder parent)
    {
        super(propertyName, parent);
        this.setMinKey(NumberFilter.MIN_KEY);
        this.setMaxKey(NumberFilter.MAX_KEY);
    }
}
