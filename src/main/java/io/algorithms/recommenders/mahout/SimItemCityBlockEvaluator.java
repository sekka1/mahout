package io.algorithms.recommenders.mahout;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.util.*;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SimItemCityBlockEvaluator {

    public static void main(String ... args){
        System.out.println(args[0]);

        // Grab input and pass it to the get method
        String action = args[0];
        String input_file = args[1];
        String input_userId = args[2];
        String input_numRec = args[3];
        String input_neighborhoodSize = args[4];

        if( action.equals( "get" ) ){
        // Run the get method
            SimItemCityBlockEvaluator simItemCityBlockEvaluator = new SimItemCityBlockEvaluator();
            String results = simItemCityBlockEvaluator.get( input_file, input_userId, input_numRec, input_neighborhoodSize );
            System.out.println( results );
        }
    }
    public String get( String input_file, String input_userId, String input_numRec, String input_neighborhoodSize)
    {
        String data = "[";
    
        //create the data model
        try{
            String recsFile = input_file;                                                                    
            long userId = Long.parseLong( input_userId );                                                    
            int numRec = Integer.parseInt( input_numRec );
            final int neighborhoodSize = Integer.parseInt( input_neighborhoodSize );

            DataModel model = new GenericBooleanPrefDataModel(
                    GenericBooleanPrefDataModel.toDataMap(
                        new FileDataModel(new File(recsFile))));

            RecommenderEvaluator evaluator =                                                                 
                new AverageAbsoluteDifferenceRecommenderEvaluator();                                         
            RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {                               
                @Override                                                                                    
                    public Recommender buildRecommender(DataModel model) throws TasteException {             
                        UserSimilarity similarity = new CityBlockSimilarity(model);                      
                        UserNeighborhood neighborhood =                                                      
                            new NearestNUserNeighborhood(neighborhoodSize, similarity, model);               
                        return new GenericBooleanPrefUserBasedRecommender(model, neighborhood, similarity);  
                    }                                                                                        
            };                                                                                               
                                                                                                             
            DataModelBuilder modelBuilder = new DataModelBuilder() {                                         
                @Override                                                                                    
                    public DataModel buildDataModel(FastByIDMap<PreferenceArray> trainingData) {             
                        return new GenericBooleanPrefDataModel(                                              
                                GenericBooleanPrefDataModel.toDataMap(trainingData));                        
                    }                                                                                        
            };

            // Run the evaluator to get the results                                                          
            double score = evaluator.evaluate(                                                               
                    recommenderBuilder, modelBuilder, model, 0.9, 1.0);

            data += "{\"score\":\"" + score + "\"}";

        }catch(Exception e){                                                                                                          
            System.out.println("gkan error" + e.getMessage() );                                                                        
        }
    
        data += "]";

        return data;
    }
}
