/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
*/
package io.algorithms.runner;

import io.algorithms.util.IOUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Wrapper implementing jersey for REST calls.
 * @author Rajiv
 */
public class WrapperServlet extends HttpServlet {
    static final long serialVersionUID = 1L;
    static final String REQUEST_PARAMETER_AUTHTOKEN = "authToken",
            REQUEST_PARAMETER_ALGOSERVER = "algoServer",
            REQUEST_PARAMETER_CLASS = "class",
            REQUEST_PARAMETER_METHOD = "method",
            REQUEST_PARAMETER_PARAMETERS = "parameters",
            PARAMETER_KEY_DATATYPE = "datatype",
            PARAMETER_KEY_VALUE = "value",
            PARAMETER_VALUE_DATASOURCE = "datasource",
            ALGO_SERVER_DEFAULT = "https://v1.api.algorithms.io";

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        process(req, resp);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        process(req, resp);
    }

    @SuppressWarnings("unchecked")
    void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // extract request parameters
        String authToken = req.getParameter(REQUEST_PARAMETER_AUTHTOKEN);
        String algoServer = req.getParameter(REQUEST_PARAMETER_ALGOSERVER);
        if (algoServer == null) { algoServer = ALGO_SERVER_DEFAULT; }
        String clazz = req.getParameter(REQUEST_PARAMETER_CLASS);
        String method = req.getParameter(REQUEST_PARAMETER_METHOD);
        String parameters = req.getParameter(REQUEST_PARAMETER_PARAMETERS);

        // Fail if some required ones are missing
        if (clazz == null) throw new ServletException("Missing required parameter 'class'");
        if (method == null) throw new ServletException("Missing required parameter 'method'");

        Map<String, Object> paramMap = null;
        Class<?>[] parameterTypeArray = null;
        String[] parameterNameArray = null;
        Object[] parameterValueArray = null;
        ObjectMapper mapper = new ObjectMapper();

        // If there are any parameters, create the name, type, and value arrays 
        if (parameters != null) {
            paramMap = mapper.readValue(parameters, Map.class);
            if (paramMap != null) {
                parameterTypeArray = new Class<?>[paramMap.size()];
                parameterNameArray = new String[paramMap.size()]; 
                parameterValueArray = new Object[paramMap.size()];
                int i = 0;
                for (String paramName : paramMap.keySet()) {
                    Map<String, Object> paramData = (Map<String, Object>) paramMap.get(paramName);
                    String type = (String) paramData.get(PARAMETER_KEY_DATATYPE);
                    Object value = paramData.get(PARAMETER_KEY_VALUE);
                    if (type != null && type.equals(PARAMETER_VALUE_DATASOURCE)) {
                         if (authToken == null) throw new ServletException("Missing required parameter 'authToken'");
                         value = IOUtils.downloadFileFromAPI(authToken, algoServer, (String) value);
                    }
                    parameterNameArray[i] = paramName;
                    parameterTypeArray[i] = IOUtils.getClazz(type);
                    parameterValueArray[i] = value;
                    i++;
                }
            }
        }
       
        // Test if the class exists and is instantiable. Exit if it doesn't.
        Class<?> c = null;
        Object instance = null;
        try {
            c = Class.forName(clazz);
            instance = c.newInstance();
        } catch (Exception e) {
            throw new ServletException(e);
        }

        // For all subsets of the parameter list (longest to none) try to see if there is a matching method
        // If not, throw an exception and exit
        Method m = null;
        boolean found = false;
        if (parameterTypeArray == null) {
            try {
                m = c.getDeclaredMethod(method);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else {
            for (int i = parameterTypeArray.length; i > 0; i--) {
                try {
                    m = c.getDeclaredMethod(method, parameterTypeArray);
                    m.setAccessible(true);
                    found = true;
                    break;
                } catch (Exception e) {
                    parameterTypeArray = Arrays.copyOfRange(parameterTypeArray, 0, i-1);
                    parameterValueArray = Arrays.copyOfRange(parameterValueArray, 0, i-1);
                }
            }
            if (!found) { throw new ServletException("Could not find method " + method + " in class " + clazz
                    + " that matches the supplied parameter types. Tried all subsets"); }
        }
        
        // All set. Now execute the method and return the results encoded as json.
        try {
            Object o = m.invoke(instance, parameterValueArray);
            byte[] out = mapper.writeValueAsBytes(o);
            resp.setHeader(IOUtils.CONTENT_TYPE, IOUtils.CONTENT_TYPE_JSON);
            resp.getOutputStream().write(out);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
