/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.builder;

/**
 * Builder class for a StringFilter.
 *
 * @version $Id$
 */
public class ListFilterBuilder extends AbstractFilterBuilder<String>
{
    /**
     * Constructor.
     * @param propertyName the name of the property
     * @param parent the parent query builder
     */
    public ListFilterBuilder(String propertyName, DocumentSearchBuilder parent)
    {
        super(propertyName, parent);
    }
}
