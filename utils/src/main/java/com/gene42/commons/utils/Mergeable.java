/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils;

/**
 * Interface defining objects which can be merged into each other.
 *
 * @param <T> class of the merger
 * @version $Id$
 */
public interface Mergeable<T>
{
    /**
     * Merges the given object into this one.
     * @param toMergeWith the object to merge into this one
     * @return this object (the resulting object merged, not toMergeWith)
     */
    Mergeable<T> merge(Mergeable<T> toMergeWith);

    /**
     * Should always return this object.
     * @return this object
     */
    T get();
}
