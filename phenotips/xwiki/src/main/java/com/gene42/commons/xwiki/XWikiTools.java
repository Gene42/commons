package com.gene42.commons.xwiki;

import org.xwiki.model.reference.EntityReference;

import java.util.LinkedList;
import java.util.List;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Utilities for XWiki related code.
 *
 * @version $Id$
 */
public final class XWikiTools
{
    private XWikiTools()
    {
    }

    /**
     * Wrapper for the document.getXObjects() method. Regardless of input or result, it will never return null.
     *
     * @param document the XWikiDocument to search for objects
     * @param objectType the object type to search for
     * @return a non empty BaseObject List if objects were found, otherwise an empty one
     */
    public static List<BaseObject> getXObjects(XWikiDocument document, EntityReference objectType)
    {
        List<BaseObject> result = null;

        if (document != null && objectType != null) {
            result = document.getXObjects(objectType);
        }

        if (result == null) {
            result = new LinkedList<>();
        }

        return result;
    }
}
