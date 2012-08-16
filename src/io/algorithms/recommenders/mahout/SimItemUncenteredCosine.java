package io.algorithms.recommenders.mahout;

import org.apache.commons.cli2.OptionException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.AbstractItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

//import net.sf.json-lib.*;

public class SimItemUncenteredCosine {

    public static void main(String ... args){
        System.out.println(args[0]);

        // Grab input and pass it to the get method
        String action = args[0];
        String input_file = args[1];
        String input_itemId = args[2];
        String input_numRec = args[3];

        if( action.equals( "get" ) ){
        // Run the get method
        	SimItemUncenteredCosine simItemUncenteredCosine = new SimItemUncenteredCosine();
            String results = simItemUncenteredCosine.get( input_file, input_itemId, input_numRec );
            System.out.println( results );
        }
    }
    public String get( String input_file, String input_itemId, String input_numRec )
    {
        String data = "[";

        try{
            String recsFile = input_file;
            long itemId = Long.parseLong( input_itemId );
            int numRec = Integer.parseInt( input_numRec );

            //create the data model
            FileDataModel dataModel = new FileDataModel(new File(recsFile));
            //Create an ItemSimilarity
            ItemSimilarity itemSimilarity = new UncenteredCosineSimilarity(dataModel);
            //Create an Item Based Recommender
            ItemBasedRecommender recommender =
                new GenericItemBasedRecommender(dataModel, itemSimilarity);

            //Get the recommendations                                                                     
            List<RecommendedItem> recommendations = recommender.mostSimilarItems(itemId, numRec);

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
