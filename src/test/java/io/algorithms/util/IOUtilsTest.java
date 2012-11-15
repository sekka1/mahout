/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
*/
package io.algorithms.util;

import static org.junit.Assert.*;

import java.io.File;

import io.algorithms.util.IOUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests IOUtils.
 * @author Rajiv
 */
public class IOUtilsTest {
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link io.algorithms.util.IOUtils#downloadFileFromAPI(java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testDownloadFileFromAPI() throws Exception {
        String algoServer = "http://pod1.staging.v1.api.algorithms.io";
        String authToken = "3bb0a4cc58933d2f18acdce2084cb387";
        String dataSourceId = "2740";
        File output = IOUtils.downloadFileFromAPI(authToken, algoServer, dataSourceId);
        assertTrue(output.exists() && output.length() > 0);
    }

    /**
     * Test method for {@link io.algorithms.util.IOUtils#getClazz(java.lang.String)}.
     */
    @Test
    public void testGetClazz() {
        assertTrue(IOUtils.getClazz(null).equals(String.class));
        assertTrue(IOUtils.getClazz("string").equals(String.class));
        assertTrue(IOUtils.getClazz("number").equals(Double.class));
        assertTrue(IOUtils.getClazz("integer").equals(Integer.class));
        assertTrue(IOUtils.getClazz("datasource").equals(File.class));
    }

}
