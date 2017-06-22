/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;

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
    private static final Map<Level, LoggerMethod> LOGGER_METHOD = new EnumMap<>(Level.class);

    static {
        LOGGER_METHOD.put(Level.ERROR, Logger::error);
        LOGGER_METHOD.put(Level.WARN, Logger::warn);
        LOGGER_METHOD.put(Level.INFO, Logger::info);
        LOGGER_METHOD.put(Level.DEBUG, Logger::debug);
        LOGGER_METHOD.put(Level.TRACE, Logger::trace);
    }

    private static final String DEFAULT_LOG_STRING = "%s: (%s)";

    private static final List<Level> LEVELS = Arrays.asList(Level.INFO, Level.ERROR, Level.WARN);

    private String logStringVariable = "%s";

    private String name;
    private Map<Level, Map<SummaryEntry, Long>> messageCountMap = new TreeMap<>();
    private int maxMessagesKept = 20;

    private Map<Level, String> logStringMap = new TreeMap<>();
    private Map<Level, String> levelHeaderMap = new TreeMap<>();
    private List<Level> levelLogOrder = LEVELS;
    private char padChar = ' ';
    private int leftPaddingSize = 2;
    private boolean alignByName = true;

    /**
     * Constructor.
     */
    public Summary()
    {
        LEVELS.forEach(level -> this.logStringMap.put(level, getLogFormattingString(null, this.logStringVariable)));
        this.levelHeaderMap.put(Level.INFO, "Summary");
        this.levelHeaderMap.put(Level.ERROR, "Errors/Warnings (# of times encountered)");
        this.insertionOrdered();
    }

    /**
     * Constructor.
     * @param name the name of the Summary. It will be appended to every message. If 2 Summary objects merge, they must
     *              have the exact same name for their messages to aggregate exactly (for equivalent messages)
     */
    public Summary(String name)
    {
        this();
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
     * Setter for leftPaddingSize.
     *
     * @param leftPaddingSize the value to set
     * @return this object
     */
    public Summary setLeftPaddingSize(int leftPaddingSize)
    {
        this.leftPaddingSize = leftPaddingSize;
        return this;
    }

    /**
     * Setter for padChar.
     *
     * @param padChar the value to set
     * @return this object
     */
    public Summary setPadChar(char padChar)
    {
        this.padChar = padChar;
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
     * Sets the formatting string for the message and its count (for the given Level).
     * @param level the level to set the string for
     * @param logFormattingString the formatting string
     * @return this object
     */
    public Summary setLogFormattingString(Level level, String logFormattingString)
    {
        if (level != null && logFormattingString != null
            && StringUtils.countMatches(logFormattingString, this.logStringVariable) == 2) {
            this.logStringMap.put(level, getLogFormattingString(logFormattingString, this.logStringVariable));
        }
        return this;
    }

    /**
     * Sets the header string to log before entries of the given level. If either argument is null, the current
     * entry is removed, and no header is logged.
     * @param level the level this header belongs to
     * @param headerString the header string to log
     * @return this object
     */
    public Summary setLogLevelHeader(Level level, String headerString)
    {
        if (level == null || headerString == null) {
            this.levelHeaderMap.remove(level);
        } else {
            this.levelHeaderMap.put(level, headerString);
        }
        return this;
    }

    /**
     * Setter for alignByName.
     *
     * @param alignByName the value to set
     * @return this object
     */
    public Summary setAlignByName(boolean alignByName)
    {
        this.alignByName = alignByName;
        return this;
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
        this.logStringMap.forEach((key, value) ->
            this.logStringMap.put(key, getLogFormattingString(value, this.logStringVariable)));
        return this;
    }

    /**
     * Setter for levelLogOrder.
     *
     * @param levelLogOrder levelLogOrder to set
     * @return this object
     */
    public Summary setLevelLogOrder(List<Level> levelLogOrder)
    {
        this.levelLogOrder = levelLogOrder;
        return this;
    }

    /**
     * Returns the message map of a specific type.
     * @param type the type of message
     * @return the messageCountMap for that type
     */
    public Map<SummaryEntry, Long> getMessageCountMap(Level type)
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

        for (Map.Entry<Level, Map<SummaryEntry, Long>> otherEntry : other.messageCountMap.entrySet()) {
            for (Map.Entry<SummaryEntry, Long> otherMapEntry : otherEntry.getValue().entrySet()) {
                this.incrementMessageCount(
                    otherEntry.getKey(), otherMapEntry.getKey(), otherMapEntry.getValue());
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
    public Summary info(String message, long amount)
    {
        return this.incrementMessageCount(Level.INFO, new SummaryEntry(message, this.name), amount);
    }

    /**
     * Increments the value of the given warn message by the 1. If it is not initialized it initializes the
     * key to 1, as long as the number of keys in the map is less then the max allowed.
     * @param message the key of the value to increment
     * @return this object
     */
    public Summary warn(String message)
    {
        return this.incrementMessageCount(Level.WARN, new SummaryEntry(message, this.name), 1);
    }

    /**
     * Increments the value of the given error message by the 1. If it is not initialized it initializes the
     * key to 1, as long as the number of keys in the map is less then the max allowed.
     * @param message the key of the value to increment
     * @return this object
     */
    public Summary error(String message)
    {
        return this.incrementMessageCount(Level.ERROR, new SummaryEntry(message, this.name), 1);
    }

    /**
     * Logs this object's messages (of the given level) with the given Logger.
     * @param level the level of the messages to log
     * @param logger the Logger to use
     * @return this object
     */
    public Summary log(Level level, Logger logger)
    {
        return log(level, logger, null);
    }

    /**
     * Logs this object with the given Logger.
     * @param logger the Logger to use
     * @return this object
     */
    public Summary log(Logger logger)
    {
        if (this.levelLogOrder != null) {
            List<SummaryEntry> allSummaryEntries = new LinkedList<>();
            this.messageCountMap.values().forEach(map -> allSummaryEntries.addAll(map.keySet()));

            final Integer namePaddingSize;
            if (this.alignByName) {
                SummaryEntry max =
                    Collections.max(allSummaryEntries, Comparator.comparing(SummaryEntry::getNameLength));
                namePaddingSize = max.getNameLength();
            } else {
                namePaddingSize = null;
            }


            this.levelLogOrder.forEach(level -> this.log(level, logger, namePaddingSize));
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


    private Summary log(Level level, Logger logger, Integer maxNameLength)
    {
        Map<SummaryEntry, Long> messageMap = this.messageCountMap.get(level);
        LoggerMethod loggerMethod = LOGGER_METHOD.get(level);

        String header = this.levelHeaderMap.get(level);

        if (header != null) {
            logger.info(header);
        }

        if (messageMap.size() > 0) {
            for (Map.Entry<SummaryEntry, Long> entry : messageMap.entrySet()) {
                loggerMethod.log(
                    logger,
                     StringUtils.repeat(this.padChar, this.leftPaddingSize) + this.logStringMap.get(level),
                    entry.getKey().toString(maxNameLength, this.padChar),
                    entry.getValue()
                );
            }
        }

        return this;
    }

    private static String getLogFormattingString(String formattingString, String logStringVariable)
    {
        if (formattingString == null) {
            return String.format(DEFAULT_LOG_STRING, logStringVariable, logStringVariable);
        } else {
            return String.format(formattingString, logStringVariable, logStringVariable);
        }
    }

    /**
     * Increments the value of the given error key by the given amount. If it is not initialized it initializes the
     * key to the amount, as long as the number of keys in the map is less then the max allowed.
     * @param summaryEntry the key of the value to increment
     * @param amount the amount to increment by
     * @return this object
     */
    private Summary incrementMessageCount(Level type, SummaryEntry summaryEntry, long amount)
    {
        Map<SummaryEntry, Long> map = this.messageCountMap.get(type);
        Long currentCount = map.get(summaryEntry);
        if (currentCount == null) {
            if (map.size() < this.maxMessagesKept) {
                map.put(summaryEntry, amount);
            }
        } else {
            map.put(summaryEntry, currentCount + amount);
        }

        return this;
    }

    @FunctionalInterface
    private interface LoggerMethod
    {
        void log(Logger logger, String message, Object o1, Object o2);
    }

    private static class SummaryEntry implements Comparable<SummaryEntry>
    {
        private final String key;
        private final String message;
        private final String name;

        SummaryEntry(String message, String name)
        {
            this.message = message;
            this.name = name;
            if (name == null) {
                this.key = message;
            } else {
                this.key = name + message;
            }
        }

        /**
         * Getter for message.
         *
         * @return message
         */
        public String getMessage()
        {
            return this.message;
        }

        /**
         * Getter for name.
         *
         * @return name
         */
        public String getName()
        {
            return this.name;
        }

        int getNameLength()
        {
            if (this.name == null) {
                return 0;
            } else {
                return this.name.length();
            }
        }

        public String toString(Integer maxNameLength, char padChar)
        {
            if (this.name == null) {
                return this.message;
            } else {
                if (maxNameLength == null) {
                    return this.name + " - " + this.message;
                } else {
                    return StringUtils.leftPad(this.name, maxNameLength, padChar) + " - " + this.message;
                }
            }
        }

        @Override
        public int hashCode()
        {
            return this.key.hashCode();
        }

        @Override
        public boolean equals(Object other)
        {
            return other instanceof SummaryEntry && this.key.equals(((SummaryEntry) other).key);
        }

        @Override
        public int compareTo(@Nonnull SummaryEntry summaryEntry)
        {
            return this.key.compareTo(summaryEntry.key);
        }
    }
}
