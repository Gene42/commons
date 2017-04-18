/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal.filter;

import org.phenotips.data.api.internal.PropertyName;
import org.phenotips.data.api.internal.QueryBuffer;

import java.util.List;

import org.json.JSONObject;

import com.xpn.xwiki.objects.PropertyInterface;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Filter checking for the existence of ojjects of a specific class in a document.
 *
 * @version $Id$
 */
public class ObjectFilter extends AbstractFilter<String>
{

    /** The type of this filter. */
    public static final String TYPE = "class";

    /** The property name for this filter. It doesn't need it. */
    public static final String DEFAULT_PROPERTY_NAME = "objFilter";

    /**
     * Constructor.
     * @param property PropertyInterface
     * @param baseClass BaseClass
     */
    public ObjectFilter(PropertyInterface property, BaseClass baseClass)
    {
        super(property, baseClass, "StringProperty");
    }

    @Override
    public QueryBuffer addValueConditions(QueryBuffer where, List<Object> bindingValues)
    {
        return this.bindPropertyClass(where, bindingValues);
    }

    @Override
    public QueryBuffer bindPropertyClass(QueryBuffer where, List<Object> bindingValues)
    {
        String baseObj;

        if (this.getParentExpression().isOrMode()) {
            baseObj = this.getParent().getObjectName(this.getParentExpression().getSpaceAndClass());
        } else {
            baseObj = this.getParent().getObjectName(this.getSpaceAndClass());
        }

        where.appendOperator().saveAndReset().startGroup();
        where.append(baseObj).append(".className=? ");

        bindingValues.add(this.getSpaceAndClass().get());

        return where.endGroup().load();
    }

    @Override
    public JSONObject prepInput(JSONObject input)
    {
        JSONObject preppedInput = new JSONObject(input.toString());
        preppedInput.put(PropertyName.PROPERTY_NAME_KEY, DEFAULT_PROPERTY_NAME);
        return preppedInput;
    }

    @Override
    public boolean isDocumentProperty()
    {
        return false;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Override
    public boolean validatesQuery()
    {
        return true;
    }
}
