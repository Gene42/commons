/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.internal.filter.NumberFilter;

/**
 * Builder class for a NumberFilter.
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
