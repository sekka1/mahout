package io.algorithms.recommenders.mahout;

import org.apache.commons.cli2.OptionException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

import java.util.*;
import java.io.*;

public class SimItemTanimotoCoefficient {

    public static void main(String ... args){
        System.out.println(args[0]);

        // Grab input and pass it to the get method
        String action = args[0];
        String input_file = args[1];
        String input_itemId = args[2];
        String input_numRec = args[3];

        if( action.equals( "get" ) ){
        // Run the get method
            SimItemLogLikelihoodNoPref simItemLogLikelihoodNoPref = new SimItemLogLikelihoodNoPref();
            String results = simItemLogLikelihoodNoPref.get( input_file, input_itemId, input_numRec );
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

            //Create an ItemSimilarity
            ItemSimilarity itemSimilarity = new TanimotoCoefficientSimilarity(model);

            //Create an Item Based Recommender
            ItemBasedRecommender recommender =
                //new GenericItemBasedRecommender(model, itemSimilarity);
                new GenericBooleanPrefItemBasedRecommender(model, itemSimilarity);

            //Get the recommendations                                                                     
            List<RecommendedItem> recommendations = recommender.mostSimilarItems(itemId, numRec);

            for (RecommendedItem item : recommendations) {
                Comparable<?> theItem = item.getItemID();

                data += "{\"id\":\""+theItem+"\",\"value\":\""+item.getValue()+"\"},";
            }

            data = data.substring(0, data.length() - 1);

        }catch(Exception e){                                                                                                          
            System.out.println("Error" + e.getMessage() );                                                                        
        }
    
        data += "]";

        return data;
    }
}
