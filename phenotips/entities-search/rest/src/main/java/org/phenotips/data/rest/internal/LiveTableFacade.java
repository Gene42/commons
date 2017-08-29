/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package org.phenotips.data.rest.internal;

import org.phenotips.data.api.internal.SpaceAndClass;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Role;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
@Role
@Component(roles = { LiveTableFacade.class })
@Singleton
public class LiveTableFacade
{
    @Inject
    private UserManager users;

    @Inject
    private AuthorizationManager access;

    @Inject
    private Provider<XWikiContext> xContextProvider;

    /** Fills in missing reference fields with those from the current context document to create a full reference. */
    @Inject
    @Named("current")
    private EntityReferenceResolver<EntityReference> currentResolver;

    public Map<String, List<String>> getQueryParameters()
    {
        XWikiRequest xwikiRequest = this.xContextProvider.get().getRequest();
        HttpServletRequest httpServletRequest = xwikiRequest.getHttpServletRequest();
        return RequestUtils.getQueryParameters(httpServletRequest.getQueryString());
    }

    public void authorizeEntitySearchInput(JSONObject inputObject)
    {
        SpaceAndClass spaceAndClass = new SpaceAndClass(inputObject);

        User currentUser = this.users.getCurrentUser();

        EntityReference spaceRef = new EntityReference(spaceAndClass.getSpaceName(), EntityType.SPACE);

        if (!this.access.hasAccess(Right.VIEW, currentUser == null ? null : currentUser.getProfileDocument(),
            this.currentResolver.resolve(spaceRef, EntityType.SPACE))) {
            throw new SecurityException(String.format("User [%s] is not authorized to access this data", currentUser));
        }
    }
}
