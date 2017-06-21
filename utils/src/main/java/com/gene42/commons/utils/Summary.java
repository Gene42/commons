/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * A summary of info/warnings/errors encountered during a task.
 *
 * @version $Id$
 */
public class Summary implements Mergeable<Summary>
{
    private static final String LOG_STRING = "%s: %s times";

    private static final List<Level> LEVELS = Arrays.asList(Level.ERROR, Level.WARN, Level.INFO);

    private String logStringVariable = "%s";
    private String logString = getLogString(this.logStringVariable);

    private String name;
    private Map<Level, Map<String, Integer>> messageCountMap = new TreeMap<>();
    private int maxMessagesKept = 20;

    /**
     * Constructor.
     */
    public Summary()
    {
        this(null);
    }

    /**
     * Constructor.
     * @param name the name of the Summary. It will be appended to every message. If 2 Summary objects merge, they must
     *              have the exact same name for their messages to aggregate exactly (for equivalent messages)
     */
    public Summary(String name)
    {
        this.name = name;
    }

    /**
     * Orders the messages in the order they were added.
     * @return this object
     */
    public Summary insertionOrdered()
    {
        LEVELS.forEach(key -> this.messageCountMap.put(key, new LinkedHashMap<>()));
        return this;
    }

    /**
     * Sorts messages by their value (used as keys in the map).
     * @return this object
     */
    public Summary sortedByKey()
    {
        LEVELS.forEach(key -> this.messageCountMap.put(key, new TreeMap<>()));
        return this;
    }

    /**
     * Getter for maxMessagesKept.
     *
     * @return the maxMessagesKept value
     */
    public int getMaxMessagesKept()
    {
        return this.maxMessagesKept;
    }

    /**
     * Setter for maxMessagesKept.
     *
     * @param maxMessagesKept the value to set
     * @return this object
     */
    public Summary setMaxMessagesKept(int maxMessagesKept)
    {
        this.maxMessagesKept = maxMessagesKept;
        return this;
    }

    /**
     * Getter for logStringVariable.
     *
     * @return logStringVariable
     */
    public String getLogStringVariable()
    {
        return this.logStringVariable;
    }

    /**
     * Setter for logStringVariable.
     *
     * @param logStringVariable logStringVariable to set
     * @return this object
     */
    public Summary setLogStringVariable(String logStringVariable)
    {
        this.logStringVariable = logStringVariable;
        this.logString = getLogString(this.logStringVariable);
        return this;
    }

    /**
     * Returns the message map of a specific type.
     * @param type the type of message
     * @return the messageCountMap for that type
     */
    public Map<String, Integer> getMessageCountMap(Level type)
    {
        return this.messageCountMap.get(type);
    }

    @Override
    public Mergeable<Summary> merge(Mergeable<Summary> toMergeWith)
    {
        if (toMergeWith == null) {
            return this;
        }

        Summary other = toMergeWith.get();

        this.maxMessagesKept += other.maxMessagesKept;

        for (Map.Entry<Level, Map<String, Integer>> otherEntry : other.messageCountMap.entrySet()) {

            for (Map.Entry<String, Integer> otherMapEntry : otherEntry.getValue().entrySet()) {
                this.incrementMessageCount(otherEntry.getKey(), otherMapEntry.getKey(), otherMapEntry.getValue());
            }
        }

        return this;
    }

    @Override
    public Summary get()
    {
        return this;
    }

    /**
     * Increments the value of the given info message by the 1. If it is not initialized it initializes the
     * key to 1, as long as the number of keys in the map is less then the max allowed.
     * @param message the key of the value to increment
     * @return this object
     */
    public Summary info(String message)
    {
        return this.info(message, 1);
    }

    /**
     * Increments the value of the given info message by the amount. If it is not initialized it initializes the
     * key to the amount, as long as the number of keys in the map is less then the max allowed.
     * @param message the key of the value to increment
     * @param amount the amount by which to increment the count of this message by
     * @return this object
     */
    public Summary info(String message, int amount)
    {
        return this.incrementMessageCount(Level.INFO, message, amount);
    }

    /**
     * Increments the value of the given warn message by the 1. If it is not initialized it initializes the
     * key to 1, as long as the number of keys in the map is less then the max allowed.
     * @param message the key of the value to increment
     * @return this object
     */
    public Summary warn(String message)
    {
        return this.incrementMessageCount(Level.WARN, message, 1);
    }

    /**
     * Increments the value of the given error message by the 1. If it is not initialized it initializes the
     * key to 1, as long as the number of keys in the map is less then the max allowed.
     * @param message the key of the value to increment
     * @return this object
     */
    public Summary error(String message)
    {
        return this.incrementMessageCount(Level.ERROR, message, 1);
    }

    /**
     * Logs this object with the given Logger.
     * @param logger the Logger to use
     * @return this object
     */
    public Summary log(Logger logger)
    {
        Map<String, Integer> error = this.messageCountMap.get(Level.ERROR);
        if (error.size() > 0) {
            for (Map.Entry<String, Integer> entry : error.entrySet()) {
                logger.error(this.logString, entry.getKey(), entry.getValue());
            }
        }

        Map<String, Integer> warn = this.messageCountMap.get(Level.WARN);
        if (warn.size() > 0) {
            for (Map.Entry<String, Integer> entry : warn.entrySet()) {
                logger.warn(this.logString, entry.getKey(), entry.getValue());
            }
        }

        Map<String, Integer> info = this.messageCountMap.get(Level.INFO);

        if (info.size() > 0) {
            for (Map.Entry<String, Integer> entry : info.entrySet()) {
                logger.info(this.logString, entry.getKey(), entry.getValue());
            }
        }

        return this;
    }

    /**
     * Logs the given Summary object with the given Logger.
     * @param logger the Logger to use
     * @param summary the Summary to log
     */
    public static void logSummary(Logger logger, Summary summary)
    {
        if (summary == null) {
            return;
        }

        summary.log(logger);
    }

    private static String getLogString(String logStringVariable)
    {
        return String.format(LOG_STRING, logStringVariable, logStringVariable);
    }

    /**
     * Increments the value of the given error key by the given amount. If it is not initialized it initializes the
     * key to the amount, as long as the number of keys in the map is less then the max allowed.
     * @param key the key of the value to increment
     * @param amount the amount to increment by
     * @return this object
     */
    private Summary incrementMessageCount(Level type, String key, int amount)
    {
        Map<String, Integer> map = this.messageCountMap.get(type);

        String internalKey;
        if (StringUtils.isBlank(this.name)) {
            internalKey = key;
        } else {
            internalKey = this.name + " - " + key;
        }

        Integer currentCount = map.get(internalKey);
        if (currentCount == null) {
            if (map.size() < this.maxMessagesKept) {
                map.put(internalKey, amount);
            }
        } else {
            map.put(internalKey, currentCount + amount);
        }

        return this;
    }
}
