/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

import org.phenotips.data.api.internal.filter.ReferenceClassFilter;

/**
 * Builder class for a StringFilter.
 *
 * @version $Id$
 */
public class ReferenceFilterBuilder extends StringFilterBuilder
{
    /**
     * Constructor.
     *
     * @param propertyName the name of the property
     * @param parent       the parent query builder
     */
    public ReferenceFilterBuilder(String propertyName, DocumentSearchBuilder parent) {
        super(propertyName, parent);
        this.setType(ReferenceClassFilter.TYPE);
    }
}
