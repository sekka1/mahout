package io.algorithms.recommenders.runner;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

//import io.algorithms.clustering.mahout.SimClustering;
import io.algorithms.recommenders.mahout.*;
import io.algorithms.utils.OS;

public class MahoutTaste extends HttpServlet {
	
	
	
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        if (request.getParameter("path_type") != null && request.getParameter("path_type").equals("windows"))
        {
        	OS.getInstance().setIsWin(true);
        }
        String dataFilePath = OS.getInstance().isWin() ? ".\\data\\" : "/opt/Data-Sets/Automation/";
        
        String action = request.getParameter("action");
        
        
        /*
        if( action.equals( "Cluster" ) ){
            
        	// Get Inputs
            SimClustering simClustering = new SimClustering();
            String outputString = simClustering.get();
            
            // Output results
            this.output( response, outputString );
        }
        */
        
        if( action.equals( "SimItemCityBlock" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemCityBlock simItemCityBlock = new SimItemCityBlock();
            String outputString = simItemCityBlock.get( recsFile, itemId, numRec );
            
            // Output results
            this.output( response, outputString );
        }
        if( action.equals( "SimItemCityBlockEvaluator" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            String neighborhoodSize;
            if (request.getParameter("neighborhoodSize") != null)
            	neighborhoodSize = request.getParameter("neighborhoodSize");
            else
            	neighborhoodSize = "10";
            // Run Action
            SimItemCityBlockEvaluator simItemCityBlockEvaluator = new SimItemCityBlockEvaluator();
            String outputString = simItemCityBlockEvaluator.get( recsFile, itemId, numRec, neighborhoodSize );
            // Output results
            this.output( response, outputString );
        }
        if( action.equals( "SimItemEuclideanDistance" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemEuclideanDistance simItemEuclideanDistance = new SimItemEuclideanDistance();
            String outputString = simItemEuclideanDistance.get( recsFile, itemId, numRec );
            // Output results
            this.output( response, outputString );
        }
        if( action.equals( "SimItemEuclideanDistanceEvaluator" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemEuclideanDistanceEvaluator simItemEuclideanDistanceEvaluator = new SimItemEuclideanDistanceEvaluator();
            String outputString = simItemEuclideanDistanceEvaluator.get( recsFile, itemId, numRec );
            // Output results
            this.output( response, outputString );
        }
        
        if( action.equals( "SimItemUncenteredCosine" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemUncenteredCosine simItemUncenteredCosine = new SimItemUncenteredCosine();
            String outputString = simItemUncenteredCosine.get( recsFile, itemId, numRec );
            // Output results
            this.output( response, outputString );
        }
        if( action.equals( "SimItemUncenteredCosineEvaluator" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemUncenteredCosineEvaluator simItemUncenteredCosineEvaluator = new SimItemUncenteredCosineEvaluator();
            String outputString = simItemUncenteredCosineEvaluator.get( recsFile, itemId, numRec );
            // Output results
            this.output( response, outputString );
        }
        
        if( action.equals( "SimItemTanimotoCoefficient" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String itemId = request.getParameter("itemId");
            String numRec = request.getParameter("numRec");
            // Run Action
            SimItemTanimotoCoefficient simItemTanimotoCoefficient = new SimItemTanimotoCoefficient();
            String outputString = simItemTanimotoCoefficient.get( recsFile, itemId, numRec );
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
        if( action.equals( "SimUserPearsonCorrelationEvaluator" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String userId = request.getParameter("userId");
            String numRec = request.getParameter("numRec");
            
            //not using neighboorhoodsize since its a set value of 10 originally from repo
            String neighborhoodSize = request.getParameter("neighborhoodSize");
            
            // Run Action
            SimUserPearsonCorrelationEvaluator simUserPearsonCorrelationEvaluator = new SimUserPearsonCorrelationEvaluator();
            String outputString = simUserPearsonCorrelationEvaluator.get( recsFile, userId, numRec );
            // Output results
            this.output( response, outputString );                                                           
        }
        if( action.equals( "SimUserSpearmanCorrelation" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String userId = request.getParameter("userId");
            String numRec = request.getParameter("numRec");
            //not using neighboorhoodsize since its a set value of 10 originally from repo
            String neighborhoodSize = request.getParameter("neighborhoodSize");
            // Run Action
            SimUserSpearmanCorrelation simUserSpearmanCorrelation = new SimUserSpearmanCorrelation();
            String outputString = simUserSpearmanCorrelation.get( recsFile, userId, numRec, neighborhoodSize );
            // Output results
            this.output( response, outputString );                                                           
        }
        if( action.equals( "SimUserSpearmanCorrelationEvaluator" ) ){

            // Get Inputs
            String recsFile = dataFilePath + request.getParameter("file");
            String userId = request.getParameter("userId");
            String numRec = request.getParameter("numRec");
            String neighborhoodSize = request.getParameter("neighborhoodSize");
            // Run Action
            //not using neighboorhoodsize since its a set value of 10 originally from repo
            SimUserSpearmanCorrelationEvaluator simUserSpearmanCorrelationEvaluator = new SimUserSpearmanCorrelationEvaluator();
            String outputString = simUserSpearmanCorrelationEvaluator.get( recsFile, userId, numRec );
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
