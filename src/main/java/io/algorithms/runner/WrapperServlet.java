/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: Feb 22, 2012$
*/
package io.algorithms.runner;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Wrapper implementing jersey for REST calls.
 */
public class WrapperServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Map<String, Class<?>> TYPES = new HashMap<String, Class<?>>();
    static {
        TYPES.put("string", String.class);
        TYPES.put("float", Double.class);
        TYPES.put("int", Integer.class);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        execute(req, resp);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        execute(req, resp);
    }

    @SuppressWarnings("unchecked")
    private void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String authToken = req.getParameter("authToken");
        String algoServer = req.getParameter("algoServer");
        String clazz = req.getParameter("class");
        String method = req.getParameter("method");
        String parameters = req.getParameter("parameters");
        ObjectMapper mapper = new ObjectMapper();

        if (clazz == null) throw new ServletException("Missing required parameter 'class'");
        if (method == null) throw new ServletException("Missing required parameter 'method'");
        Map<String, Object> paramMap = null;
        Class<?>[] parameterTypeArray = null;
        String[] parameterNameArray = null;
        Object[] parameterValueArray = null;
        if (parameters != null) {
            paramMap = mapper.readValue(parameters, Map.class);
            if (paramMap != null) {
                parameterTypeArray = new Class<?>[paramMap.size()];
                parameterNameArray = new String[paramMap.size()]; 
                parameterValueArray = new Object[paramMap.size()];
                int i = 0;
                for (String paramName : paramMap.keySet()) {
                    Map<String, Object> paramData = (Map<String, Object>) paramMap.get(paramName);
                    parameterTypeArray[i] = getClazz((String) paramData.get("datatype"));
                    parameterNameArray[i] = paramName;
                    parameterValueArray[i] = paramData.get("value");
                    i++;
                }
            }
        }
        Class<?> c = null;
        Object instance = null;
        Method m = null;
        try {
            c = Class.forName(clazz);
            instance = c.newInstance();
            m = c.getDeclaredMethod(method, parameterTypeArray);
            m.setAccessible(true);
            Object o = m.invoke(instance, parameterValueArray);
            byte[] out = mapper.writeValueAsBytes(o);
            resp.setHeader("Content-Type", "application/json");
            resp.getOutputStream().write(out);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    private static Class<?> getClazz(String type) {
        if (type == null) { type = "string"; }
        return TYPES.get(type);
    }
}
