/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest.internal;

import com.gene42.commons.utils.exceptions.ServiceException;

import org.phenotips.data.api.EntitySearch;
import org.phenotips.data.api.EntitySearchResult;
import org.phenotips.data.rest.LiveTableGenerator;
import org.phenotips.data.rest.LiveTableRowHandler;
import org.phenotips.data.rest.LiveTableSearch;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DocumentLiveTableGenerator implements LiveTableGenerator<DocumentReference>
{
    @Inject
    private LiveTableRowHandler responseRowHandler;

    @Inject
    private DocumentAccessBridge documentAccessBridge;

    @Override
    public JSONObject generateTable(EntitySearchResult<DocumentReference> documentSearchResult, JSONObject inputObject,
        Map<String, List<String>> queryParameters) throws ServiceException
    {
        JSONObject responseObject = new JSONObject();

        JSONArray rows = new JSONArray();
        responseObject.put(LiveTableSearch.Keys.ROWS, rows);

        List<TableColumn> cols = this.getColumns(inputObject);
        try {
            for (DocumentReference docRef : documentSearchResult.getItems()) {
                JSONObject row = this.responseRowHandler.getRow(this.getDocument(docRef), cols, queryParameters);
                if (row != null) {
                    rows.put(row);
                }
            }
        } catch (XWikiException e) {
            throw new ServiceException(e);
        }

        responseObject.put(EntitySearch.Keys.REQUEST_NUMBER_KEY, Long.valueOf(RequestUtils.getFirst(queryParameters,
            EntitySearch.Keys.REQUEST_NUMBER_KEY, "0")));

        responseObject.put(LiveTableSearch.Keys.TOTAL_ROWS, documentSearchResult.getTotalRows());
        responseObject.put(LiveTableSearch.Keys.RETURNED_ROWS, documentSearchResult.getReturnedRows());
        responseObject.put(LiveTableSearch.Keys.OFFSET, documentSearchResult.getOffset() + 1);

        return responseObject;
    }

    private List<TableColumn> getColumns(JSONObject jsonObject)
    {
        if (jsonObject.optJSONArray(EntitySearch.Keys.COLUMN_LIST_KEY) == null) {
            throw new IllegalArgumentException(String.format("No %1$s key found.",
                EntitySearch.Keys.COLUMN_LIST_KEY));
        }

        JSONArray columnArray = jsonObject.getJSONArray(EntitySearch.Keys.COLUMN_LIST_KEY);

        List<TableColumn> columns = new LinkedList<>();

        for (Object obj : columnArray) {
            if (!(obj instanceof JSONObject)) {
                throw new IllegalArgumentException(String.format("Column %1$s is not a JSONObject", obj));
            }

            columns.add(new TableColumn().populate((JSONObject) obj));
        }

        return columns;
    }

    private XWikiDocument getDocument(DocumentReference docRef) throws XWikiException
    {
        if (docRef == null) {
            return null;
        }

        try {
            return (XWikiDocument) this.documentAccessBridge.getDocument(docRef);

        } catch (Exception e) {
            throw new XWikiException("Error while getting document " + docRef.getName(), e);
        }
    }
}
