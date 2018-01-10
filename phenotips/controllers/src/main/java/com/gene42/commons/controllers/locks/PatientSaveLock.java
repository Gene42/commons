package com.gene42.commons.controllers.locks;

import org.xwiki.component.annotation.Component;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */

@Component(roles = LockWithTimeout.class)
@Named(PatientSaveLock.NAME)
@Singleton
public class PatientSaveLock extends AbstractLockWithTimeout
{
    /** Name. */
    public static final String NAME = "patient";

    @Override
    public String getName()
    {
        return NAME;
    }
}
