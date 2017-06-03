/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.exceptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class for ServiceException.
 *
 * @version $Id$
 */
public class ServiceExceptionTest
{
    @Test
    public void testBasicConstructors()
    {
        try {
            wrapException(new ServiceException());
        } catch (ServiceException e) {
            assertEquals(e.getStatus(), ServiceException.Status.INTERNAL_EXCEPTION);
            assertEquals(e.getMessage(), null);
        }

        try {
            throw new ServiceException(ServiceException.Status.DATA_ALREADY_EXISTS);
        } catch (ServiceException e) {
            assertEquals(e.getStatus(), ServiceException.Status.DATA_ALREADY_EXISTS);
            assertEquals(e.getMessage(), null);
        }
    }

    @Test
    public void testOtherConstructors()
    {
        Exception throwable = new Exception("testing");
        try {
            throw new ServiceException(ServiceException.Status.COULD_NOT_SAVE_DATA, throwable);
        } catch (ServiceException e) {
            assertEquals(e.getStatus(), ServiceException.Status.COULD_NOT_SAVE_DATA);
            assertEquals(e.getCause(), throwable);
            assertEquals(e.getMessage(), "testing");
        }

        try {
            throw new ServiceException("test 123", throwable);
        } catch (ServiceException e) {
            assertEquals(e.getStatus(), ServiceException.Status.INTERNAL_EXCEPTION);
            assertEquals(e.getCause(), throwable);
            assertEquals(e.getMessage(), "test 123");
        }
    }

    @Test
    public void testStatusTransferConstructors()
    {
        try {
            wrapException(new ServiceException(ServiceException.Status.SERVICE_UNAVAILABLE, "test"));
        } catch (ServiceException e) {
            assertEquals(e.getStatus(), ServiceException.Status.SERVICE_UNAVAILABLE);
        }

        try {
            wrapException(new ServiceException("test"));
        } catch (ServiceException e) {
            assertEquals(e.getStatus(), ServiceException.Status.INTERNAL_EXCEPTION);
        }

        try {
            wrapException(new Exception("test2"));
        } catch (ServiceException e) {
            assertEquals(e.getStatus(), ServiceException.Status.INTERNAL_EXCEPTION);
            assertEquals(e.getMessage(), "test2");
        }
    }

    public static void wrapException(Exception e) throws ServiceException
    {
        try {
            throw e;
        } catch (Exception e1) {
            throw new ServiceException(e1);
        }
    }
}
