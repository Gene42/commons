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
     * @param parent the parent query builder
     */
    public NumberFilterBuilder(DocumentSearchBuilder parent)
    {
        super(parent);
        this.setMinKey(NumberFilter.MIN_KEY);
        this.setMaxKey(NumberFilter.MAX_KEY);
    }
}
