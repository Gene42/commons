package com.gene42.commons.utils;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.gene42.commons.utils.exceptions.ServiceException;

/**
 * General Utilities.
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

    /**
     * Puts the key/value pair in the given map iff both key and value are not null.
     * @param key the key
     * @param value the value
     * @param map the map to put the key/value pair
     * @param <K> the type of the key
     * @param <V> the type of the value
     * @return the previous value associated with key, or null if there was no mapping for key.
     *         (A null return can also indicate that the map previously associated null with key,
     *          if the implementation supports null values.). If either key or value is null this will return null.
     */
    public static <K, V> V putIgnoreNull(K key, V value, @NotNull Map<K, V> map) {
        if (key != null && value != null) {
            return map.put(key, value);
        }

        return null;
    }

    /**
     * Get a float from the given object. If it cannot be parsed either the defaultValue is returned or an
     * exception is thrown, based on what the value of the failIfInvalid field is.
     * @param value the value to convert
     * @param failIfInvalid if true and parsing fails an exception is thrown
     * @param defaultValue default value in case the given value is null or cannot be parsed
     * @return a Float or null
     * @throws ServiceException if an exception happens during parsing and failIfInvalid is true
     */
    @Contract("_, true, _ -> !null")
    public static Float getFloat(final Object value, boolean failIfInvalid, final Float defaultValue)
        throws ServiceException {

        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }

        Float result = null;
        NumberFormatException formatException = null;

        try {
            String str = getStringValue(value, true, null);
            if (str != null) {
                result = Float.parseFloat(str);
            }
        } catch (NumberFormatException e) {
            formatException = e;
        }

        if (result == null) {
            result = defaultValue;
        }

        if (failIfInvalid && (formatException != null || result == null)) {
            String errorMessage = String.format("Error while converting [%s] to a float", value);
            if (formatException == null) {
                throw new ServiceException(errorMessage);
            } else {
                throw new ServiceException(errorMessage, formatException);
            }
        }

        return result;
    }
}
