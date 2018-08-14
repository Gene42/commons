package com.gene42.commons.data;

import org.xwiki.component.annotation.Role;

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
    T create(T object) throws ServiceException;
    T create(JSONObject jsonContent)  throws ServiceException;
    T get(String id) throws ServiceException;
    T update(T object) throws ServiceException;
    T update(String id, JSONObject jsonContent) throws ServiceException;
    T delete(String id) throws ServiceException;
}
