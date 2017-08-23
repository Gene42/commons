/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest.internal.adapter;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.internal.PropertyName;
import org.phenotips.data.api.internal.SpaceAndClass;
import org.phenotips.data.api.internal.filter.AbstractFilter;
import org.phenotips.data.api.internal.filter.OrderFilter;
import org.phenotips.data.rest.LiveTableInputAdapter;
import org.phenotips.data.rest.internal.RequestUtils;
import org.phenotips.data.rest.internal.TableColumn;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class converts URL parameters given py PhenoTips frontend during table searches, into the JSONObject
 * representation which the DocumentSearch interface knows how to handle.
 *
 * @version $Id$
 */
@Component(roles = { LiveTableInputAdapter.class })
@Named("url")
@Singleton
public class URLInputAdapter implements LiveTableInputAdapter
{
    private static final String VALUE_DELIMITER = ",";

    private static final String CLASS_NAME_KEY = "classname";

    private static final Set<String> NON_FILTERS = new HashSet<>();


    private static final String OUTPUT_SYNTAX_KEY = "outputSyntax";
    private static final String FILTER_WHERE_KEY = "filterWhere";
    private static final String FILTER_FROM_KEY = "filterFrom";
    private static final String QUERY_FILTERS_KEY = "queryFilters";

    static {
        NON_FILTERS.add(CLASS_NAME_KEY);
        NON_FILTERS.add(EntitySearch.Keys.LIMIT_KEY);
        NON_FILTERS.add(EntitySearch.Keys.OFFSET_KEY);
        NON_FILTERS.add(EntitySearch.Keys.SORT_KEY);
        NON_FILTERS.add(EntitySearch.Keys.REQUEST_NUMBER_KEY);
        NON_FILTERS.add(OUTPUT_SYNTAX_KEY);
        NON_FILTERS.add(FILTER_WHERE_KEY);
        NON_FILTERS.add(FILTER_FROM_KEY);
        NON_FILTERS.add(QUERY_FILTERS_KEY);
        NON_FILTERS.add(EntitySearch.Keys.SORT_DIR_KEY);
        NON_FILTERS.add(EntitySearch.Keys.COLUMN_LIST_KEY);
        NON_FILTERS.add(RequestUtils.TRANS_PREFIX_KEY);
    }

    @Override
    public JSONObject convert(Map<String, List<String>> queryParameters)
    {
        String documentClassName = RequestUtils.getFirst(queryParameters, URLInputAdapter.CLASS_NAME_KEY);


        DocumentQueryBuilder builder = new DocumentQueryBuilder(documentClassName);

        // Key is param name, value param value list
        for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
            if (!URLInputAdapter.NON_FILTERS.contains(entry.getKey())) {
                builder.addFilter(ParameterKey.FILTER_KEY_PREFIX + entry.getKey(), entry.getValue());
            }
        }

        this.addOrderFilter(builder, queryParameters);

        JSONObject queryObj = builder.build().toJSON();

        queryObj.put(EntitySearch.Keys.LIMIT_KEY, RequestUtils.getFirst(queryParameters, EntitySearch.Keys.LIMIT_KEY));
        queryObj.put(EntitySearch.Keys.OFFSET_KEY,
            Integer.parseInt(RequestUtils.getFirst(queryParameters, EntitySearch.Keys.OFFSET_KEY, "1")) - 1);
        queryObj.put(EntitySearch.Keys.COLUMN_LIST_KEY, this.getColumnList(documentClassName, queryParameters));

        return queryObj;
    }

    private void addOrderFilter(DocumentQueryBuilder builder, Map<String, List<String>> queryParameters)
    {
        String sortKey = ParameterKey.FILTER_KEY_PREFIX + RequestUtils.getFirst(queryParameters, EntitySearch.Keys
            .SORT_KEY);
        String typeKey = sortKey + ParameterKey.PROPERTY_DELIMITER + AbstractFilter.TYPE_KEY;

        builder.addToOrderFilter(sortKey,
            Collections.singletonList(RequestUtils.getFirst(queryParameters, EntitySearch.Keys.SORT_DIR_KEY)));
        builder.addToOrderFilter(typeKey, Collections.singletonList(OrderFilter.TYPE));
    }

    private JSONArray getColumnList(String className, Map<String, List<String>> queryParameters)
    {
        String [] tokens = StringUtils.split(RequestUtils.getFirst(queryParameters, EntitySearch.Keys.COLUMN_LIST_KEY),
            URLInputAdapter.VALUE_DELIMITER);

        JSONArray array = new JSONArray();

        if (tokens == null) {
            return array;
        }

        for (String token : tokens) {

            JSONObject obj = new JSONObject();
            if (StringUtils.startsWith(token, PropertyName.DOC_PROPERTY_PREFIX)) {
                obj.put(TableColumn.TYPE_KEY, EntityType.DOCUMENT.toString());
            } else {
                obj.put(TableColumn.TYPE_KEY, EntityType.OBJECT.toString());

                String key = token + ParameterKey.PROPERTY_DELIMITER + SpaceAndClass.CLASS_KEY;
                String specialKey = token + ParameterKey.PROPERTY_CLASS_SUFFIX;

                if (queryParameters.containsKey(key)) {
                    obj.put(TableColumn.CLASS_KEY, RequestUtils.getFirst(queryParameters, key));
                } else if (queryParameters.containsKey(specialKey)) {
                    obj.put(TableColumn.CLASS_KEY, RequestUtils.getFirst(queryParameters, specialKey));
                } else {
                    obj.put(TableColumn.CLASS_KEY, className);
                }
            }

            obj.put(TableColumn.COLUMN_NAME_KEY, token);

            array.put(obj);
        }

        return array;
    }
}
