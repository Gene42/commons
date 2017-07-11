/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.xwiki;

import org.phenotips.Constants;
import org.phenotips.security.authorization.AuthorizationService;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;

import com.gene42.commons.utils.exceptions.ServiceException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Utilities for XWiki related code.
 *
 * @version $Id$
 */
@Component(roles = XWikiTools.class)
@Named("default")
@Singleton
public final class XWikiTools
{
    @Inject
    private UserManager users;

    @Inject
    @Named("default")
    private DocumentReferenceResolver<EntityReference> resolver;

    @Inject
    private AuthorizationService authorizationService;

    @Inject
    private Provider<XWikiContext> contextProvider;

    /**
     * Returns whether or not the current user is an Admin or not.
     * @return true if current user is an Admin, false otherwise
     */
    public boolean isCurrentUserAdmin()
    {
        return isUserAdmin(this.users.getCurrentUser());
    }

    /**
     * Returns whether or not the given user is an Admin or not.
     * @param user the user to check (can be null, will return false)
     * @return true if user is an Admin, false otherwise
     */
    public boolean isUserAdmin(User user)
    {
        if (user == null || !user.exists()) {
            return false;
        } else {
            return this.authorizationService.hasAccess(user, Right.ADMIN,
                this.resolver.resolve(Constants.XWIKI_SPACE_REFERENCE));
        }
    }

    /**
     * Returns a list of groups names which the given user belongs to. A max of 10k results will be returned.
     * @param user the user whose groups to search for
     * @return a List of string names
     * @throws ServiceException if any error happens while retrieving the group names
     */
    public List<String> getGroupsUserBelongsTo(User user) throws ServiceException
    {
        try {
            XWikiContext context = this.contextProvider.get();
            Collection<String> groups = context.getWiki()
                .getGroupService(context)
                .getAllGroupsNamesForMember(user.getProfileDocument().toString(), 10000, 0, context);

            if (CollectionUtils.isNotEmpty(groups)) {
                return new ArrayList<>(groups);
            }
        } catch (XWikiException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * Wrapper for the document.getXObjects() method. Regardless of input or result, it will never return null.
     *
     * @param document the XWikiDocument to search for objects
     * @param objectType the object type to search for
     * @return a non empty BaseObject List if objects were found, otherwise an empty one
     */
    public static List<BaseObject> getXObjects(XWikiDocument document, EntityReference objectType)
    {
        List<BaseObject> result = null;

        if (document != null && objectType != null) {
            result = document.getXObjects(objectType);
        }

        if (result == null) {
            result = new LinkedList<>();
        }

        return result;
    }
}
