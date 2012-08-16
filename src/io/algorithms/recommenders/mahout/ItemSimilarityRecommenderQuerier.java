/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.recommenders.mahout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.GenericItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.GenericItemSimilarity.ItemItemSimilarity;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;

import io.algorithms.entity.Algorithm;
import io.algorithms.entity.AlgorithmException;
import io.algorithms.entity.DataSetEntity;
import io.algorithms.entity.DataSetEntityBase;
import io.algorithms.entity.InvalidDataSetException;
import io.algorithms.entity.InvalidParameterException;

/**
 * Trains an item based recommender.
 */
public class ItemSimilarityRecommenderQuerier implements Algorithm {
    public static final String NUM_REC = "numRec", ITEM_ID = "itemId";

    /* (non-Javadoc)
     * @see io.algorithms.entity.Algorithm#getImplementationClass()
     */
    @Override
    public String getImplementationClass() {
        return "Algorithms_simItemNoPrefQuerier";
    }

    /* (non-Javadoc)
     * @see io.algorithms.entity.Algorithm#validate(java.util.List, java.util.Properties)
     */
    @Override
    public List<DataSetEntity> validate(List<DataSetEntity> inputDataSets,
            Properties parameters) throws InvalidDataSetException,
            InvalidParameterException, IOException {
        if (inputDataSets == null || inputDataSets.size() != 2) {
            throw new InvalidDataSetException("Missing Input Data");
        }
        if (parameters == null || parameters.getProperty(NUM_REC) == null || parameters.getProperty(ITEM_ID) == null) {
            throw new InvalidParameterException("Missing one or more required parameters [" + NUM_REC + "," + ITEM_ID + "]");
        }
        
        try {
            Integer.parseInt(parameters.getProperty(NUM_REC));
            Long.parseLong(parameters.getProperty(ITEM_ID));
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e);
        }

        // TODO: More validation on input dataset to ensure it's a valid matrix
        
        // TODO: Return a real looking output dataset
        return inputDataSets;
    }

    /* (non-Javadoc)
     * @see io.algorithms.entity.Algorithm#run(java.util.List, java.util.Properties)
     */
    @Override
    public List<DataSetEntity> run(List<DataSetEntity> inputDataSets,
            Properties parameters) throws InvalidDataSetException,
            InvalidParameterException, IOException, AlgorithmException {
        validate(inputDataSets, parameters);

        DataSetEntity originalData = inputDataSets.get(0), itemSimilarityMatrix = inputDataSets.get(1);
        List<DataSetEntity> outputList = new ArrayList<DataSetEntity>(1);
        
        int numRec = Integer.parseInt(parameters.getProperty(NUM_REC));
        long itemId = Long.parseLong(parameters.getProperty(ITEM_ID));

        BufferedReader br = new BufferedReader(new InputStreamReader(itemSimilarityMatrix.getDataInputStream()));
        String line;
        List<ItemItemSimilarity> itemItemSimilarity = new ArrayList<ItemItemSimilarity>();
        int i = 0, j = 0;
        while ((line = br.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            while (tokenizer.hasMoreTokens()) {
                double tokenValue = Double.parseDouble(tokenizer.nextToken());
                if (tokenValue >= -1 && tokenValue <= 1) {
                    itemItemSimilarity.add(new ItemItemSimilarity(i+1, j+1, tokenValue));
                }
                j++;
            }
            i++;
            j = 0;
        }
        
//        DataModel fileDataModel = new FileDataModel(originalData.getDataFile());
        ItemBasedRecommender itemBasedRecommender = new GenericBooleanPrefItemBasedRecommender(null, new GenericItemSimilarity(itemItemSimilarity));
        try {
            List<RecommendedItem> recommendations = itemBasedRecommender.mostSimilarItems(itemId, numRec);
            // create a temp file with the output
            File outputFile = new File("recommendations." + originalData.getFileSystemName() + "." + System.currentTimeMillis());
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
            for (RecommendedItem recommendedItem : recommendations) {
                StringBuilder sb = new StringBuilder().append(recommendedItem.getItemID()).append(",").append(recommendedItem.getValue()).append("\n");
                bw.write(sb.toString());
            }
            
            // now create output data set
            DataSetEntityBase output = new DataSetEntityBase();
            output.setName(outputFile.getName());
            output.setFileSystemName(outputFile.getName());
            output.setLocation(originalData.getLocation());
            output.persist();
            output.putDataFile(outputFile); // push
            outputList.add(output);
            
        } catch (TasteException e) {
            throw new AlgorithmException(e);
        }
        return outputList;
    }

}
