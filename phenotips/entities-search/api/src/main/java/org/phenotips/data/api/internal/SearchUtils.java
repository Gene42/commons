/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.api.internal;

import org.phenotips.Constants;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class providing JSONObject retrieval functions and EntityReference helper methods.
 *
 * @version $Id$
 */
public final class SearchUtils
{

    /** Allowed values for boolean true. */
    public static final Set<String> BOOLEAN_TRUE_SET = UnmodifiableSet.unmodifiableSet(
        new HashSet<>(Arrays.asList("yes", "true", "1")));

    /** Allowed values for boolean false. */
    public static final Set<String> BOOLEAN_FALSE_SET = UnmodifiableSet.unmodifiableSet(
        new HashSet<>(Arrays.asList("no", "false", "0")));

    private SearchUtils()
    {
        // Private constructor for util class
    }

    /**
     * Returns a DocumentReference given a period delimited space and class name.
     * @param spaceAndClass space and class "Space.Class"
     * @return a DocumentReference (never null)
     */
    public static EntityReference getClassReference(String spaceAndClass)
    {

        String [] tokens = SpaceAndClass.getSpaceAndClass(spaceAndClass);

        if (tokens.length == 2) {
            return getClassReference(tokens[0], tokens[1]);
        } else {
            return new EntityReference(spaceAndClass, EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);
        }

    }

    /**
     * Returns an EntityReference given the space reference and className (the class of the document).
     * @param spaceRef the SpaceReference to use
     * @param className the class name to use
     * @return a EntityReference
     */
    public static EntityReference getClassReference(SpaceReference spaceRef, String className)
    {
        EntityReference reference = new EntityReference(className, EntityType.DOCUMENT);
        return new EntityReference(reference, spaceRef);
    }

    /**
     * Returns an EntityReference given the space name and className (the class of the document).
     * @param space the name of the space to use
     * @param className the class name to use
     * @return a EntityReference
     */
    public static EntityReference getClassReference(String space, String className)
    {
        SpaceReference parent = new SpaceReference(space, new WikiReference("xwiki"));
        return getClassReference(parent, className);
    }

    /**
     * Returns a DocumentReference given the space and class string. This string needs to be in the format
     * [space name].[class name]
     *
     * Example: PhenoTips.VisibilityClass
     *
     * @param spaceAndClass a period delimited space and class name string
     * @return a EntityReference
     */
    public static DocumentReference getClassDocumentReference(String spaceAndClass)
    {
        return new DocumentReference(getClassReference(spaceAndClass));
    }

    /**
     * Helper function for negating a comparison operator. If the negate flag is false, the given operator is returned.
     * If it is true the following happens. If the operator is '=', the result is '!='. If the operator is a null
     * comparison 'is null' the result is ' is not null '. All other operators are negated as follows, if operator is
     * 'x' the result is ' not x '. Notice the extra spaces added before and after the operators, except when dealing
     * with the equals operator.
     *
     * @param operator the operator to process
     * @param negate flag for enabling negation
     * @return a String with the operator
     */
    public static String getComparisonOperator(String operator, boolean negate)
    {
        if (negate) {
            if (StringUtils.equals(operator, "=")) {
                return "!=";
            } else if (StringUtils.equals(operator, "is null")) {
                return " is not null ";
            } else {
                return " not " + operator + " ";
            }
        } else {
            return " " + operator + " ";
        }
    }

    /**
     * Returns a Set of names of filter parameters which store filter values.
     * Examples:
     * values, min, max, before, after ... etc
     *
     * @return a Set of Strings
     */
    public static Set<String> getValueParameterNames()
    {
        return new DefaultFilterFactory(null).getValueParameterNames();
    }

    /**
     * Returns the the string value of the object. If the object is an array or collection the string value of the
     * first item in the array is returned.
     * @param object the object to get the string value of
     * @return a String
     */
    public static String getFirstString(Object object)
    {
        String result = StringUtils.EMPTY;

        if ((object instanceof Object []) && ArrayUtils.isNotEmpty((Object[]) object)) {
            Object[] array = (Object[]) object;
            result = String.valueOf(array[0]);
        } else if ((object instanceof Collection) && CollectionUtils.isNotEmpty((Collection) object)) {
            result = String.valueOf(((Collection) object).iterator().next());
        } else if (object != null) {
            return String.valueOf(object);
        }

        return result;
    }
}
