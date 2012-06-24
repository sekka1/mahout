package io.algorithms.recommenders.runner;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import io.algorithms.recommenders.mahout.*;

public class MahoutTaste extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        //String dataFilePath = "/opt/Data-Sets/Automation/";
    	String dataFilePath = "./data/";
        //Forward Slashes can't be used by windows
        
        if (System.getProperty("os.name").toLowerCase().contains("win") || request.getParameter("path_type").equals("windows"))
        	dataFilePath = dataFilePath.replace("/", "\\");
        
        String action = request.getParameter("action");
        if( action.equals( "SimItemTanimotoCoefficient" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemLogLikelihood simItemLogLikelihood = new SimItemLogLikelihood();
            String outputString = simItemLogLikelihood.get( recsFile, itemId, numRec );
            // Output results
            this.output( response, outputString );
        }
        if( action.equals( "SimItemLogLikelihood" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemLogLikelihood simItemLogLikelihood = new SimItemLogLikelihood();
            String outputString = simItemLogLikelihood.get( recsFile, itemId, numRec );
            // Output results
            this.output( response, outputString );
        }
        if( action.equals( "SimItemLogLikelihoodNoPref" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemLogLikelihoodNoPref simItemLogLikelihoodNoPref = new SimItemLogLikelihoodNoPref();
            String outputString = simItemLogLikelihoodNoPref.get( recsFile, itemId, numRec );
            // Output results
            this.output( response, outputString );
        }
        if( action.equals( "SimItemLogLikelihoodNoPrefEvaluator" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemLogLikelihoodNoPrefEvaluator simItemLogLikelihoodNoPrefEvaluator = new SimItemLogLikelihoodNoPrefEvaluator();
            String outputString = simItemLogLikelihoodNoPrefEvaluator.get( recsFile, itemId, numRec );
            // Output results
            this.output( response, outputString );
        }
        if( action.equals( "SimUserLogLikelihood" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String userId = request.getParameter("userId");
            String numRec = request.getParameter("numRec");
            String neighborhoodSize = request.getParameter("neighborhoodSize");
            // Run Action
            SimUserLogLikelihood simUserLogLikelihood = new SimUserLogLikelihood();
            String outputString = simUserLogLikelihood.get( recsFile, userId, numRec, neighborhoodSize );
            // Output results
            this.output( response, outputString );
        }
        if( action.equals( "SimUserPearsonCorrelation" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String userId = request.getParameter("userId");
            String numRec = request.getParameter("numRec");
            String neighborhoodSize = request.getParameter("neighborhoodSize");
            // Run Action
            SimUserPearsonCorrelation simUserPearsonCorrelation = new SimUserPearsonCorrelation();
            String outputString = simUserPearsonCorrelation.get( recsFile, userId, numRec, neighborhoodSize );
            // Output results
            this.output( response, outputString );                                                           
        }
        if( action.equals( "SimUserLogLikelihoodNoPref" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String userId = request.getParameter("userId");
            String numRec = request.getParameter("numRec");
            String neighborhoodSize = request.getParameter("neighborhoodSize");
            // Run Action
            SimUserLogLikelihoodNoPref simUserLogLikelihoodNoPref = new SimUserLogLikelihoodNoPref();
            String outputString = simUserLogLikelihoodNoPref.get( recsFile, userId, numRec, neighborhoodSize );
            // Output results
            this.output( response, outputString );                                                           
        }
        if( action.equals( "SimUserLogLikelihoodNoPrefEvaluator" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String userId = request.getParameter("userId");
            String numRec = request.getParameter("numRec");
            String neighborhoodSize = request.getParameter("neighborhoodSize");
            // Run Action
            SimUserLogLikelihoodNoPrefEvaluator simUserLogLikelihoodNoPrefEvaluator = new SimUserLogLikelihoodNoPrefEvaluator();
            String outputString = simUserLogLikelihoodNoPrefEvaluator.get( recsFile, userId, numRec, neighborhoodSize );
            // Output results
            this.output( response, outputString );
        }

    }
    private void output( HttpServletResponse response, String outputString ){

        try{
            response.setContentType("text/html");

            PrintWriter out = response.getWriter();

            out.println( outputString );

        }catch(Exception e){
            System.out.println("Error: " + e.getMessage() );                                                         }
    }
}
