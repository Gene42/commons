package com.gene42.commons.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public final class Utils
{
    private Utils() {
        // Do nothing
    }

    /**
     * Returns the string value of the given object. If the object is null, the defaultValue is returned.
     * If the string value is the empty string, if considerEmptyNull is set to true, the defaultValue is returned,
     * otherwise the empty string.
     *
     * @param obj the object to convert
     * @param considerEmptyNull if the value of the string is the empty, it is considered null and the defaultValue
     *                          applies
     * @param defaultValue default value if object is null (or empty)
     * @return the string representation
     */
    public static String getStringValue(Object obj, boolean considerEmptyNull, String defaultValue)
    {
        if (obj == null) {
            return defaultValue;
        }

        String str = String.valueOf(obj);
        if (StringUtils.isEmpty(str)) {
            return (considerEmptyNull) ? defaultValue : str;
        }

        return str;
    }
}
