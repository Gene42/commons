package com.gene42.commons.xwiki.data;

import org.phenotips.data.api.EntitySearchResult;

import org.xwiki.component.annotation.Role;

import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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

    @NotNull
    T update(@NotNull T object) throws ServiceException;

    @NotNull
    T update(@NotNull String id, @NotNull JSONObject jsonContent) throws ServiceException;

    @NotNull
    T delete(@NotNull String id) throws ServiceException;

    @NotNull
    T delete(@NotNull T object) throws ServiceException;

    @NotNull
    EntitySearchResult<String> search(List<String> filters, List<String> sorts, int offset, int limit)
            throws ServiceException;
}
