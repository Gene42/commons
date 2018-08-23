package com.gene42.commons.xwiki.data;

import org.phenotips.data.api.EntitySearchResult;

import org.xwiki.component.annotation.Role;

import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import com.gene42.commons.utils.exceptions.ServiceException;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
@Role
public interface ResourceFacade<T extends RestResource>
{
    @NotNull
    T create(@NotNull T object) throws ServiceException;

    @NotNull
    T create(@NotNull JSONObject jsonContent)  throws ServiceException;

    @Contract("_, true -> !null")
    T get(@NotNull String id, boolean failIfNotFound) throws ServiceException;

    @Contract("_, true -> !null")
    T update(@NotNull T object, boolean failIfNotFound) throws ServiceException;

    @Contract("_, _, true -> !null")
    T update(@NotNull String id, @NotNull JSONObject jsonContent, boolean failIfNotFound) throws ServiceException;

    @Contract("_, true -> !null")
    T delete(@NotNull String id, boolean failIfNotFound) throws ServiceException;

    @Contract("_, true -> !null")
    T delete(@NotNull T object, boolean failIfNotTrue) throws ServiceException;

    @NotNull
    EntitySearchResult<String> search(List<String> filters, List<String> sorts, int offset, int limit)
            throws ServiceException;

    boolean hasAccess(@NotNull ResourceOperation operation, @Nullable T resource, boolean failIfNotTrue)
        throws ServiceException;

    boolean hasAccess(@NotNull ResourceOperation operation, @Nullable String id, boolean failIfNotTrue)
        throws ServiceException;
}
