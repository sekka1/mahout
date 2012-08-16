package io.algorithms.recommenders.mahout;

import org.apache.commons.cli2.OptionException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.eval.*;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.eval.*;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SimItemLogLikelihoodNoPrefEvaluator {

    public static void main(String ... args){
        System.out.println(args[0]);

        // Grab input and pass it to the get method
        String action = args[0];
        String input_file = args[1];
        String input_itemId = args[2];
        String input_numRec = args[3];

        if( action.equals( "get" ) ){
        // Run the get method
            SimItemLogLikelihoodNoPrefEvaluator simItemLogLikelihoodNoPrefEvaluator = new SimItemLogLikelihoodNoPrefEvaluator();
            String results = simItemLogLikelihoodNoPrefEvaluator.get( input_file, input_itemId, input_numRec );
            System.out.println( results );
        }
    }
    public String get( String input_file, String input_itemId, String input_numRec )
    {
        String data = "[";
    
        //create the data model
        try{
            String recsFile = input_file;                                                                    
            long itemId = Long.parseLong( input_itemId );                                                    
            int numRec = Integer.parseInt( input_numRec );

            DataModel model = new GenericBooleanPrefDataModel(
                    GenericBooleanPrefDataModel.toDataMap(
                        new FileDataModel(new File(recsFile))));

            RecommenderEvaluator evaluator =
                new AverageAbsoluteDifferenceRecommenderEvaluator();
            RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {
                @Override
                    public Recommender buildRecommender(DataModel model) throws TasteException {
                        ItemSimilarity similarity = new LogLikelihoodSimilarity(model);
                        //UserNeighborhood neighborhood =
                        //    new NearestNUserNeighborhood(10, similarity, model);
                        return new GenericBooleanPrefItemBasedRecommender(model, similarity);
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
