/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.xwiki.data;

import com.gene42.commons.utils.json.JSONafiable;

/**
 * Interface describing a resource to be made available through a REST interface.
 *
 * @version $Id$
 */
public interface RestResource extends JSONafiable
{
    /**
     * Return the unique id of the resource. Should be UUID.
     * @return the id
     */
    String getId();

    /**
     * Return the rest resource type. For example 'patient', 'cohort', 'family', etc..
     * @return the type
     */
    String getResourceType();
}
