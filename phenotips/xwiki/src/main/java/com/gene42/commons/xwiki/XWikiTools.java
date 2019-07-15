/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.xwiki;

import com.gene42.commons.utils.exceptions.ServiceException;

import org.phenotips.Constants;
import org.phenotips.security.authorization.AuthorizationService;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;
import org.xwiki.velocity.tools.EscapeTool;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiGroupService;

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
    private static final int MAX_NUM_OF_GROUPS = 1000;

    private static final String ADMINISTRATORS_SUFFIX = " Administrators";

    private static final String XWIKI = "XWiki";

    private static final String XWIKI_PREFS = "XWikiPreferences";

    private static final String GUEST = "xwikiguest";

    @Inject
    private UserManager userManager;

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
        return isUserAdmin(this.userManager.getCurrentUser());
    }

    /**
     * Checks to see if given user is a guest.
     * @param user the user to check. If null, it will return true.
     * @return true if user is a guest or given object is null
     */
    @Contract("null -> true")
    public boolean isGuest(User user)
    {
        return user == null || StringUtils.contains(StringUtils.lowerCase(user.getUsername()), GUEST);
    }

    /**
     * Getter for userManager.
     *
     * @return userManager
     */
    public UserManager getUserManager()
    {
        return this.userManager;
    }

    /**
     * Getter for resolver.
     *
     * @return resolver
     */
    public DocumentReferenceResolver<EntityReference> getResolver() {
        return this.resolver;
    }

    /**
     * Getter for authorizationService.
     *
     * @return authorizationService
     */
    public AuthorizationService getAuthorizationService() {
        return this.authorizationService;
    }

    /**
     * Getter for contextProvider.
     *
     * @return contextProvider
     */
    public Provider<XWikiContext> getContextProvider() {
        return this.contextProvider;
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
     * Returns a list of groups names which the given user belongs to. A max of {@value XWikiTools#MAX_NUM_OF_GROUPS}
     * results will be returned.
     * @param user the user whose groups to search for
     * @return a List of string names
     * @throws ServiceException if any error happens while retrieving the group names
     */
    public Set<String> getGroupsUserBelongsTo(User user) throws ServiceException
    {
        try {

            XWikiContext context = this.contextProvider.get();
            XWiki wiki = context.getWiki();
            XWikiGroupService groupService = wiki.getGroupService(context);

            return getGroupsRecursive(
                user.getProfileDocument(),
                new HashSet<>(),
                context,
                groupService,
                new EscapeTool(),
                false
            );
        } catch (XWikiException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    private static Set<String> getGroupsRecursive(DocumentReference entity, Set<String> groupSet, XWikiContext
        context, XWikiGroupService groupService, EscapeTool escapeTool, boolean addEntity) throws XWikiException
    {
        if (groupSet.size() >= MAX_NUM_OF_GROUPS) {
            return groupSet;
        }

        String entityFullName = getSanitizedGroupName(entity.toString(), escapeTool);

        if (groupSet.contains(entityFullName)) {
            return groupSet;
        }

        if (addEntity) {
            groupSet.add(entityFullName);
        }

        Collection<DocumentReference> groups =
            groupService.getAllGroupsReferencesForMember(entity, MAX_NUM_OF_GROUPS, 0, context);

        if (CollectionUtils.isNotEmpty(groups)) {
            for (DocumentReference group : groups) {
                getGroupsRecursive(group, groupSet, context, groupService, escapeTool, true);
            }
        }

        return groupSet;
    }

    private static String getSanitizedGroupName(String groupName, EscapeTool escapeTool)
    {
        String entityFullName = escapeTool.sql(groupName);
        if (StringUtils.endsWith(entityFullName, ADMINISTRATORS_SUFFIX)) {
            return StringUtils.removeEnd(entityFullName, ADMINISTRATORS_SUFFIX);
        } else {
            return entityFullName;
        }
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

    /**
     * Allows to retrieve a list of {@link BaseObject} given the XWiki {@code context} and {@code objectType}.
     *
     * @param context the {@link XWikiContext}
     * @param objectType the {@link EntityReference}
     * @return a list of objects of type {@code objectType}
     */
    @NotNull
    public static List<BaseObject> getXObjects(@NotNull final XWikiContext context,
        @Nullable final EntityReference objectType)
    {
        final XWiki xWiki = context.getWiki();
        try {
            final DocumentReference ref = new DocumentReference(xWiki.getDatabase(), XWIKI, XWIKI_PREFS);
            final XWikiDocument prefsDoc = xWiki.getDocument(ref, context);
            return XWikiTools.getXObjects(prefsDoc, objectType);
        } catch (final XWikiException e) {
            return Collections.emptyList();
        }
    }
}
