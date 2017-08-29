/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest;

import org.phenotips.data.api.EntitySearchResult;

import org.xwiki.component.annotation.Role;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.gene42.commons.utils.exceptions.ServiceException;

/**
 * DESCRIPTION.
 * @param <T> type of the EntitySearchResult to use.
 * @version $Id$
 */
@Role
public interface LiveTableGenerator<T>
{
    /**
     * Returns a JSONObject containing the resulting table based on the given EntitySearchResult.
     * @param documentSearchResult the EntitySearchResult to use to generate the result table
     * @param inputObject the query input object
     * @param queryParameters the query parameters
     * @return a JSONObject
     * @throws ServiceException on any error
     */
    JSONObject generateTable(EntitySearchResult<T> documentSearchResult, JSONObject inputObject, Map<String,
        List<String>> queryParameters) throws ServiceException;
}
