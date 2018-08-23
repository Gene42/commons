package com.gene42.commons.xwiki.data;

import java.util.Objects;

/**
 * DESCRIPTION.
 *
 * @version $Id$
 */
public class ResourceOperation
{
    public static class Names {
        public static final String CREATE = "create";
        public static final String GET = "get";
        public static final String UPDATE = "update";
        public static final String DELETE = "delete";
        public static final String SEARCH = "search";
    }

    public static final ResourceOperation CREATE = new ResourceOperation(Names.CREATE);
    public static final ResourceOperation GET = new ResourceOperation(Names.GET);
    public static final ResourceOperation UPDATE = new ResourceOperation(Names.UPDATE);
    public static final ResourceOperation DELETE = new ResourceOperation(Names.DELETE);
    public static final ResourceOperation SEARCH = new ResourceOperation(Names.SEARCH);
    private final String name;

    public ResourceOperation(String name){
        this.name = name;
    }

    /**
     * Getter for name.
     *
     * @return name
     */
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceOperation)) {
            return false;
        }
        ResourceOperation that = (ResourceOperation) o;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.name);
    }
}
