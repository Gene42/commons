package com.gene42.commons.xwiki.data;

import com.gene42.commons.utils.json.JSONafiable;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public interface RestResource extends JSONafiable
{
    String getId();
    String getResourceType();
}
