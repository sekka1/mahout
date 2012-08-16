package io.algorithms.recommenders.mahout;

import org.apache.commons.cli2.OptionException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.recommender.*;
import org.apache.mahout.cf.taste.similarity.*;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.model.DataModel;

import java.util.*;

import java.io.*;

public class SimUserLogLikelihoodNoPref {

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
            SimUserLogLikelihood simUserLogLikelihood = new SimUserLogLikelihood();
            String results = simUserLogLikelihood.get( input_file, input_userId, input_numRec, input_neighborhoodSize );
            System.out.println( results );
        }
    }
    public String get( String input_file, String input_userId, String input_numRec, String input_neighborhoodSize )
    {
        String data = "[";

        try{
            String recsFile = input_file;
            long userId = Long.parseLong( input_userId );
            int numRec = Integer.parseInt( input_numRec );
            int neighborhoodSize = Integer.parseInt( input_neighborhoodSize );
    
            DataModel model = new GenericBooleanPrefDataModel(
                    GenericBooleanPrefDataModel.toDataMap(
                        new FileDataModel(new File(recsFile))));

            //Create an ItemSimilarity                                                                       
            UserSimilarity similarity = new LogLikelihoodSimilarity(model);                                  
                                                                                                             
            UserNeighborhood neighborhood =                                                                  
                new NearestNUserNeighborhood(neighborhoodSize, similarity, model);                           
                                                                                                             
            //Create an Item Based Recommender                                                               
            UserBasedRecommender recommender =                                                               
                //new GenericItemBasedRecommender(model, itemSimilarity);                                    
                new GenericBooleanPrefUserBasedRecommender(model, neighborhood, similarity);


            //Get the recommendations                                                                     
            List<RecommendedItem> recommendations = recommender.recommend(userId, numRec);

            for (RecommendedItem item : recommendations) {
                Comparable<?> theItem = item.getItemID();

                data += "{\"id\":\""+theItem+"\",\"value\":\""+item.getValue()+"\"},";
            }

            data = data.substring(0, data.length() - 1);

        } catch(Exception e){
            System.out.println("gkan error" + e.getMessage() );
        }

        data += "]";

        return data;

    }
}
