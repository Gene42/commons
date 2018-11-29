/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api;

import org.xwiki.component.annotation.Role;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import com.gene42.commons.utils.exceptions.ServiceException;

/**
 * Interface for searching entities in the system.
 * @param <T> result type
 * @version $Id$
 */
@Role
@FunctionalInterface
public interface EntitySearch<T>
{
    /**
     *
     * @param queryParameters the parameters used to create the query
     * @return a DocumentSearchResult containing the documents plus extra metadata
     * @throws ServiceException on any issues during document querying
     */
    @NotNull
    EntitySearchResult<T> search(JSONObject queryParameters) throws ServiceException;

    /**
     * Holder for key values.
     */
    final class Keys
    {
        /** Input key. */
        public static final String CLASS_KEY = "class";

        /** Input key. */
        public static final String LIMIT_KEY = "limit";

        /** Input key. */
        public static final String OFFSET_KEY = "offset";

        /** Input key. */
        public static final String SORT_KEY = "sort";

        /** Input key. */
        public static final String SORT_DIR_KEY = "dir";

        /** Input key. */
        public static final String REQUEST_NUMBER_KEY = "reqNo";

        /** Input key. */
        public static final String COLUMN_LIST_KEY = "collist";

        /** Input key. */
        public static final String FILTERS_KEY = "filters";

        /** Input key. */
        public static final String COUNT_ONLY = "count_only";

        private Keys()
        {
            // Empty
        }
    }
}
