/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.testing.mocking;

import java.lang.reflect.Parameter;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * {@code MockitoExtension} showcases the {@link TestInstancePostProcessor}
 * and {@link ParameterResolver} extension APIs of JUnit 5 by providing
 * dependency injection support at the field level and at the method parameter
 * level via Mockito 2.x's {@link Mock @Mock} annotation.
 *
 * @version $Id$
 */
public class JUnit5MockitoExtension implements TestInstancePostProcessor, ParameterResolver
{
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context)
    {
        MockitoAnnotations.initMocks(testInstance);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
    {
        return parameterContext.getParameter().isAnnotationPresent(Mock.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
    {
        return getMock(parameterContext.getParameter(), extensionContext);
    }

    private Object getMock(Parameter parameter, ExtensionContext extensionContext)
    {
        Class<?> mockType = parameter.getType();
        Store mocks = extensionContext.getStore(Namespace.create(JUnit5MockitoExtension.class, mockType));
        String mockName = getMockName(parameter);

        if (mockName != null) {
            return mocks.getOrComputeIfAbsent(mockName, key -> Mockito.mock(mockType, mockName));
        } else {
            return mocks.getOrComputeIfAbsent(mockType.getCanonicalName(), key -> Mockito.mock(mockType));
        }
    }

    private String getMockName(Parameter parameter)
    {
        String explicitMockName = parameter.getAnnotation(Mock.class).name().trim();
        if (!explicitMockName.isEmpty()) {
            return explicitMockName;
        } else if (parameter.isNamePresent()) {
            return parameter.getName();
        }
        return null;
    }
}
