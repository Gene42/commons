/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest.internal;

import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONObject;

import com.gene42.commons.utils.json.JSONafiable;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class LiveTableStopWatches implements JSONafiable
{
    private StopWatch adapterStopWatch = new StopWatch();
    private StopWatch searchStopWatch = new StopWatch();
    private StopWatch tableStopWatch = new StopWatch();

    /**
     * Getter for adapterStopWatch.
     *
     * @return adapterStopWatch
     */
    public StopWatch getAdapterStopWatch()
    {
        return this.adapterStopWatch;
    }

    /**
     * Getter for searchStopWatch.
     *
     * @return searchStopWatch
     */
    public StopWatch getSearchStopWatch()
    {
        return this.searchStopWatch;
    }

    /**
     * Getter for tableStopWatch.
     *
     * @return tableStopWatch
     */
    public StopWatch getTableStopWatch()
    {
        return this.tableStopWatch;
    }

    @Override
    public JSONObject toJSONObject()
    {
        JSONObject timingsJSON = new JSONObject();
        timingsJSON.put("adapter", this.getAdapterStopWatch().getTime());
        timingsJSON.put("search", this.getSearchStopWatch().getTime());
        timingsJSON.put("table", this.getTableStopWatch().getTime());
        return timingsJSON;
    }
}
