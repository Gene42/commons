/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.EntitySearchResult;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.query.internal.ScriptQuery;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.gene42.commons.utils.exceptions.ServiceException;
import com.gene42.commons.utils.json.JSONTools;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

/**
 * Handles Document Searches.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultDocumentSearchImpl implements EntitySearch<DocumentReference>
{
    private static final String LIMIT_DEFAULT = "15";

    /** Filters to add to the query. "currentlanguage", "hidden" */
    // TODO: this does not work with our doc naming convention
    private static final Set<String> QUERY_FILTER_SET = UnmodifiableSet.unmodifiableSet(
        new HashSet<>(Collections.singletonList("unique")));

    @Inject
    private QueryManager queryManager;

    @Inject
    private ComponentManager componentManager;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private Logger logger;

    @NotNull
    @Override
    public EntitySearchResult<DocumentReference> search(JSONObject queryParameters) throws ServiceException
    {
        int offset = queryParameters.optInt(EntitySearch.Keys.OFFSET_KEY);
        if (offset <= 0) {
            offset = 0;
        }

        int limit = Integer.parseInt(JSONTools.getValue(
            queryParameters, EntitySearch.Keys.LIMIT_KEY, DefaultDocumentSearchImpl.LIMIT_DEFAULT)
        );

        XWiki wiki = this.contextProvider.get().getWiki();

        ScriptQuery scriptQuery = this.getQuery(queryParameters, false, limit, offset);
        ScriptQuery countScriptQuery = this.getQuery(queryParameters, true, limit, offset);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug(String.format("[queryParameters= %1$s ]", queryParameters.toString(4)));
            this.logger.debug(String.format("[statement= %1$s ]", scriptQuery.getStatement()));
        }

        List<String> results;
        try {
            if (Boolean.valueOf(JSONTools.getValue(queryParameters, Keys.COUNT_ONLY, "false"))) {
                results = Collections.emptyList();
            } else {
                results = this.getQueryResults(scriptQuery.execute());
            }

        } catch (QueryException e) {
            throw new ServiceException(e);
        }

        return new EntitySearchResult<DocumentReference>()
            .setItems(this.getDocRefs(results, wiki.getDatabase()))
            .setOffset(offset)
            .setTotalRows(countScriptQuery.count());
    }

    private List<String> getQueryResults(List resultList)
    {
        List<String> stringResults = new LinkedList<>();

        for (Object obj : resultList) {
            stringResults.add(SearchUtils.getFirstString(obj));
        }

        return stringResults;
    }

    private List<DocumentReference> getDocRefs(List<String> docNames, String wikiName)
    {
        List<DocumentReference> result = new LinkedList<>();

        for (String docName : docNames) {
            String [] tokens = StringUtils.split(docName, ".", 2);
            result.add(new DocumentReference(wikiName, tokens[0], tokens[1]));
        }

        return result;
    }

    private ScriptQuery getQuery(JSONObject queryParameters, boolean count, int limit, int offset) throws
        ServiceException
    {
        List<Object> bindingValues = new LinkedList<>();
        DocumentQuery docQuery = new DocumentQuery(new DefaultFilterFactory(this.contextProvider), count);
        String queryStr = docQuery.init(queryParameters).hql(null, bindingValues).toString();

        Query query;
        try {
            query = this.queryManager.createQuery(queryStr, "hql");
        } catch (QueryException e) {
            throw new ServiceException(e);
        }

        ScriptQuery scriptQuery = new ScriptQuery(query, this.componentManager);

        scriptQuery.setLimit(limit);
        scriptQuery.setOffset(offset);
        scriptQuery.bindValues(bindingValues);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug(String.format("[ %1$s ]", queryStr));
            this.logger.debug(String.format("[values=%1$s ]", Arrays.toString(bindingValues.toArray())));
        }

        return scriptQuery;
    }
}
