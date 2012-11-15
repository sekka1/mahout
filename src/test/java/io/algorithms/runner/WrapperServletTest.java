/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
*/
package io.algorithms.runner;

import static org.junit.Assert.*;
import static io.algorithms.runner.WrapperServlet.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import io.algorithms.util.IOUtils;

import javax.servlet.ServletException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test for wrapper servlet.
 * @author Rajiv
 */
public class WrapperServletTest {
    private WrapperServlet servlet;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        servlet = new WrapperServlet();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        servlet = null;
        request = null;
        response = null;
    }

    /**
     * Test method for {@link io.algorithms.runner.WrapperServlet#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void negativeTestProcessNullParameters() throws Exception {
        addBoilerplateRequestParameters();
        try {
            servlet.process(request, response);
            fail("There is no such method but it didn't throw an exception");
        } catch (ServletException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }
    }

    /**
     * Test method for {@link io.algorithms.runner.WrapperServlet#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testProcess1() throws Exception {
        addBoilerplateRequestParameters();
        request.addParameter(REQUEST_PARAMETER_PARAMETERS, "{\"text\":{\"value\":\"Teams consist of diversity and compromise\",\"datatype\":\"string\"}}");
        servlet.process(request, response);
        assertNotNull(response);
        assertEquals(response.getContentType(), IOUtils.CONTENT_TYPE_JSON);
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = mapper.readValue(response.getContentAsByteArray(), Map.class);
        assertEquals(map.size(), 4);
    }
    
    /**
     * Test method for {@link io.algorithms.runner.WrapperServlet#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testProcessExtraStringParameters() throws Exception {
        addBoilerplateRequestParameters();
        request.addParameter(REQUEST_PARAMETER_PARAMETERS, "{\"text\":{\"value\":\"Teams consist of diversity and compromise\",\"datatype\":\"string\"}, \"second\":{\"value\":\"2048\"}}");
        servlet.process(request, response);
        assertNotNull(response);
        assertEquals(response.getContentType(), IOUtils.CONTENT_TYPE_JSON);
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = mapper.readValue(response.getContentAsByteArray(), Map.class);
        assertEquals(map.size(), 4);
    }
    
    /**
     * Test method for {@link io.algorithms.runner.WrapperServlet#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testProcessExtraDatasourceParameters() throws Exception {
        addBoilerplateRequestParameters();
        request.addParameter(REQUEST_PARAMETER_PARAMETERS, "{\"text\":{\"value\":\"Teams consist of diversity and compromise\",\"datatype\":\"string\"}, \"second\":{\"value\":\"2740\", \"datatype\":\"datasource\"}}");
        servlet.process(request, response);
        assertNotNull(response);
        assertEquals(response.getContentType(), IOUtils.CONTENT_TYPE_JSON);
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = mapper.readValue(response.getContentAsByteArray(), Map.class);
        assertEquals(map.size(), 4);
    }
    
    /**
     * Test method for {@link io.algorithms.runner.WrapperServlet#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     */
    @Test
    public void testProcessInvalidDatasourceParameter() throws Exception {
        addBoilerplateRequestParameters();
        request.addParameter(REQUEST_PARAMETER_PARAMETERS, "{\"second\":{\"value\":\"2740\", \"datatype\":\"datasource\"},\"text\":{\"value\":\"Teams consist of diversity and compromise\",\"datatype\":\"string\"}}");
        try {
            servlet.process(request, response);
            fail("Supplied binary file should have thrown a json exception");
        } catch (ServletException e) {
            assertTrue(e.getCause() instanceof InvocationTargetException);
        }
    }
    
    /**
     * Common to all tests.
     */
    private void addBoilerplateRequestParameters() {
        request.addParameter(REQUEST_PARAMETER_AUTHTOKEN, "3bb0a4cc58933d2f18acdce2084cb387");
        request.addParameter(REQUEST_PARAMETER_ALGOSERVER, "http://pod1.staging.v1.api.algorithms.io");
        request.addParameter(REQUEST_PARAMETER_CLASS, "io.algorithms.text.RelevantSpeechFinder");
        request.addParameter(REQUEST_PARAMETER_METHOD, "findRelevantSpeeches");
    }
}
