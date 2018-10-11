/*
 * This file is subject to the terms and conditions defined in file LICENSE,
 * which is part of this source code package.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */
package com.gene42.commons.utils.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Test class for JSONTools.
 *
 * @version $Id$
 */
public class JSONToolsTest
{
    /**
     * Class set up.
     *
     * @throws  Exception  on error
     */
    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    /**
     * Class tear down.
     *
     * @throws  Exception  on error
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test set up.
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test tear down.
     */
    @After
    public void tearDown() {
    }


    @Test
    public void getJSONArrayTest() throws Exception
    {
        String key = "key";
        JSONObject obj = new JSONObject();
        JSONArray arr = JSONTools.getJSONArray(obj, key);
        assertNotNull(arr);
    }

    @Test
    public void weqewqwe() throws Exception
    {
        Blob blob = new Blob().setId(2L).setName("Haha").setObj(new Hmm());
        //JsonApiMapper.stuff(Blob.class);
        //JsonApiMapper.map(blob, Blob.class);
    }

    @JsonApiResource(type = "blob")
    public class Blob {

        private Long id;
        private String name;
        private Hmm obj;

        /**
         * Getter for id.
         *
         * @return id
         */
        public Long getId() {
            return this.id;
        }

        /**
         * Setter for id.
         *
         * @param id id to set
         * @return this object
         */
        public Blob setId(Long id) {
            this.id = id;
            return this;
        }

        /**
         * Getter for name.
         *
         * @return name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Setter for name.
         *
         * @param name name to set
         * @return this object
         */
        public Blob setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Getter for obj.
         *
         * @return obj
         */
        public Hmm getObj() {
            return this.obj;
        }

        /**
         * Setter for obj.
         *
         * @param obj obj to set
         * @return this object
         */
        public Blob setObj(Hmm obj) {
            this.obj = obj;
            return this;
        }
    }

    public class Hmm {
        @Override
        public String toString() {
            return "Hmmmmmmmmmm";
        }
    }
}
