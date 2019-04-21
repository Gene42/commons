/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.xwiki.data;

import com.gene42.commons.utils.exceptions.ServiceException;

import org.phenotips.data.api.EntitySearchResult;

import org.xwiki.component.annotation.Role;

import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/**
 * Interface for business level facades meant to provide CRUD operations for a RestResource.
 *
 * @param <T> an object type extending the RestResource interface
 * @version $Id$
 */
@Role
public interface ResourceFacade<T extends RestResource>
{
    /**
     * Create the given RestResource.
     *
     * @param object the RestResource to create (persist)
     *
     * @return the newly created RestResource, now with its generated id available
     *
     * @throws ServiceException if any issue occurs during object creation/save
     */
    @NotNull
    T create(@NotNull T object) throws ServiceException;

    /**
     * Create a RestResource from the given json content.
     *
     * @param jsonContent JSON string representation of the RestResource to create.
     *
     * @return the newly created RestResource, now with its generated id available
     *
     * @throws ServiceException if any issue occurs during object creation/save
     */
    @NotNull
    T create(@NotNull JSONObject jsonContent) throws ServiceException;

    /**
     * Retrieve the RestResource identified by the given id.
     *
     * @param id the id of the RestResource
     * @param failIfNotFound if set to true, and the RestResource was not found an exception will be thrown.
     *                       The exception will have a {@link ServiceException.Status DATA_NOT_FOUND} status
     * @return the RestResource or null if not found (and failIfNotFound is false)
     *
     * @throws ServiceException if any issue occurs while retrieving the RestResource, or if the failIfNotFound is set
     *                          to true and the RestResource was not found
     */
    @Contract("_, true -> !null")
    T get(@NotNull String id, boolean failIfNotFound) throws ServiceException;

    /**
     * Update an existing the RestResource with the contents of the given RestResource. The id of the RestResource to
     * update will be retrieved from the given object. Only the fields present in the given object will be used for
     * updating. Any missing fields will be left intact on the existing RestResource.
     *
     * @param object a RestResource object to use for updating an existing one
     * @param failIfNotFound if set to true, and the RestResource to be updated was not found an exception will be
     *                       thrown. The exception will have a {@link ServiceException.Status DATA_NOT_FOUND} status
     *
     * @return the newly updated RestResource with all the relevant fields updated
     *
     * @throws ServiceException if any issue occurs while updating the RestResource, or if the failIfNotFound is set to
     *                          true and the RestResource was not found
     */
    @Contract("_, true -> !null")
    T update(@NotNull T object, boolean failIfNotFound) throws ServiceException;

    /**
     * Update an existing the RestResource with the contents of the given JSON content. The id of the RestResource to
     * update will be retrieved from the given JSON. Only the fields present in the given JSON will be used for
     * updating. Any missing fields will be left intact on the existing RestResource.
     *
     * @param id the id of the RestResource to update
     * @param jsonContent json string content to use for updating an existing RestResource
     * @param failIfNotFound if set to true, and the RestResource to be updated was not found an exception will be
     *                       thrown. The exception will have a {@link ServiceException.Status DATA_NOT_FOUND} status
     *
     * @return the newly updated RestResource with all the relevant fields updated
     *
     * @throws ServiceException if any issue occurs while updating the RestResource, or if the failIfNotFound is set to
     *                          true and the RestResource was not found
     */
    @Contract("_, _, true -> !null")
    T update(@NotNull String id, @NotNull JSONObject jsonContent, boolean failIfNotFound) throws ServiceException;

    /**
     * Delete the RestResource identified by the given id.
     *
     * @param id the id of the RestResource to delete
     * @param failIfNotFound if set to true, and the RestResource to be deleted was not found an exception will be
     *                       thrown. The exception will have a {@link ServiceException.Status DATA_NOT_FOUND} status
     *
     * @return the last version of the RestResource before it was deleted
     *
     * @throws ServiceException if any issue occurs while deleting the RestResource, or if the failIfNotFound is set to
     *                          true and the RestResource was not found
     */
    @Contract("_, true -> !null")
    T delete(@NotNull String id, boolean failIfNotFound) throws ServiceException;

    /**
     * Delete the given RestResource. It has to have a valid id.
     *
     * @param object the RestResource to delete
     * @param failIfNotFound if set to true, and the RestResource to be deleted was not found an exception will be
     *                       thrown. The exception will have a {@link ServiceException.Status DATA_NOT_FOUND} status
     *
     * @return the last version of the RestResource before it was deleted
     *
     * @throws ServiceException if any issue occurs while deleting the RestResource, or if the failIfNotFound is set to
     *                          true and the RestResource was not found
     */
    @Contract("_, true -> !null")
    T delete(@NotNull T object, boolean failIfNotFound) throws ServiceException;

    /**
     * Search for RestResources and filter by the given filters, sort by the given sorts and start/limit
     * the results by the given values.
     *
     * @param filters the filters to use for determining which RestResources to return. If not provided all
     *                RestResources in the system will be returned (restricted by the offset/limit values)
     * @param sorts   the fields to sort by and the direction for each
     * @param offset  the starting index of the first RestResources to return. This in combination with limit
     *                allow for pagination.
     * @param limit   the maximum number of RestResources to return in the result set
     *
     * @return        a EntitySearchResult containing the results plus extra metadata
     *
     * @throws ServiceException if any issues are encountered during the search
     */
    @NotNull
    EntitySearchResult<String> search(List<String> filters, List<String> sorts, int offset, int limit)
            throws ServiceException;

    /**
     * Checks if the user of the current request has access (described by the operation) to the given resource.
     *
     * @param operation the operation to check the access for (read, write, etc..)
     * @param resource  the RestResource to check the access against
     * @param failIfNotTrue if set to true, and the request user does not have access to the givenRestResource
     *                      an exception will be thrown.
     *                      The exception will have a {@link ServiceException.Status FORBIDDEN} status
     *
     * @return boolean true if the request user has access to the RestResource, false otherwise
     *
     * @throws ServiceException if any issue occurs while checking the access, or if the failIfNotTrue is set to
     *                          true and the request user does not have access to the RestResource
     */
    boolean hasAccess(@NotNull ResourceOperation operation, @Nullable T resource, boolean failIfNotTrue)
        throws ServiceException;

    /**
     * Checks if the user of the current request has access (described by the operation) to the RestResource
     * identified by the given id.
     *
     * @param operation the operation to check the access for (read, write, etc..)
     * @param id  the id of the RestResource to check the access against
     * @param failIfNotTrue if set to true, and the request user does not have access to the givenRestResource
     *                      an exception will be thrown.
     *                      The exception will have a {@link ServiceException.Status FORBIDDEN} status
     *
     * @return boolean true if the request user has access to the RestResource, false otherwise
     *
     * @throws ServiceException if any issue occurs while checking the access, or if the failIfNotTrue is set to
     *                          true and the request user does not have access to the RestResource
     */
    boolean hasAccess(@NotNull ResourceOperation operation, @Nullable String id, boolean failIfNotTrue)
        throws ServiceException;
}
