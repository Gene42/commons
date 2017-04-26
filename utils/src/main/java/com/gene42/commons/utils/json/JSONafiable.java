/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.json;

import org.json.JSONObject;

/**
 * An implementing class must be able to generate a JSONObject representation of itself.
 *
 * @version $Id$
 */
public interface JSONafiable
{
    /**
     * Returns a JSONObject representation of the implementing class.
     * @return a JSONObject
     */
    JSONObject toJSONObject();
}
