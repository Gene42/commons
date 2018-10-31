/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Container class for the Document Search result. Holds the list of results plus any other extra metadata related
 * to the query.
 *
 * @param <T> the type of the result objects
 *
 * @version $Id$
 */
public class EntitySearchResult<T> implements Iterable<T>
{
    private long totalRows;

    private long offset;

    private List<T> items = new LinkedList<>();

    /**
     * Getter for totalRows.
     *
     * @return totalRows
     */
    public long getTotalRows()
    {
        return this.totalRows;
    }

    /**
     * Setter for totalRows.
     *
     * @param totalRows totalRows to set
     * @return this object
     */
    public EntitySearchResult<T> setTotalRows(long totalRows)
    {
        this.totalRows = totalRows;
        return this;
    }

    /**
     * Getter for returnedRows.
     *
     * @return returnedRows
     */
    public int getReturnedRows()
    {
        return CollectionUtils.size(this.items);
    }


    /**
     * Getter for offset.
     *
     * @return offset
     */
    public long getOffset()
    {
        return this.offset;
    }

    /**
     * Setter for offset.
     *
     * @param offset offset to set
     * @return this object
     */
    public EntitySearchResult<T> setOffset(long offset)
    {
        this.offset = offset;
        return this;
    }

    /**
     * Getter for items.
     *
     * @return items
     */
    public List<T> getItems()
    {
        return this.items;
    }

    /**
     * Setter for items.
     *
     * @param items items to set
     * @return this object
     */
    public EntitySearchResult<T> setItems(List<T> items)
    {
        if (items != null) {
            this.items = items;
        }
        return this;
    }

    /**
     * Returns whether or not the result list is empty.
     * @return boolean
     */
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(this.items);
    }

    @Override
    public Iterator<T> iterator() {
        return this.items.iterator();
    }
}
