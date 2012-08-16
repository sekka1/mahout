/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.recommenders.mahout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.algorithms.entity.Algorithm;
import io.algorithms.entity.AlgorithmException;
import io.algorithms.entity.DataSetEntity;
import io.algorithms.entity.DataSetEntityBase;
import io.algorithms.entity.InvalidDataSetException;
import io.algorithms.entity.InvalidParameterException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * Trains an item based recommender.
 */
public class ItemSimilarityRecommenderTrainer implements Algorithm {
    /**
     * Different item similarity metrics.
     */
    public static enum ItemSimilarityMetric {
        LOG_LIKELIHOOD,
        TANIMOTO_COEFFICIENT
    }

    public static final String ITEM_SIMILARITY_METRIC = "ItemSimilarityMetric";
    
    
    /* (non-Javadoc)
     * @see io.algorithms.common.resource.Algorithm#validate(java.util.List, org.apache.mahout.common.Parameters)
     */
    public List<DataSetEntity> validate(List<DataSetEntity> inputDataSets,
            Properties parameters) throws InvalidDataSetException,
            InvalidParameterException {
        if (inputDataSets == null || inputDataSets.size() != 1) {
            throw new InvalidDataSetException("Missing Input Data");
        }
        if (parameters == null || parameters.getProperty(ITEM_SIMILARITY_METRIC) == null) {
            throw new InvalidParameterException("Missing one or more required parameters [" + ITEM_SIMILARITY_METRIC + "]");
        }
        
        try {
            ItemSimilarityMetric.valueOf(parameters.getProperty(ITEM_SIMILARITY_METRIC));
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException(e);
        }

        // TODO: Return a real sample output dataset
        return inputDataSets;
    }

    /* (non-Javadoc)
     * @see io.algorithms.common.resource.Algorithm#run(java.util.List, org.apache.mahout.common.Parameters)
     */
    public List<DataSetEntity> run(List<DataSetEntity> inputDataSets, Properties parameters)
            throws InvalidDataSetException, InvalidParameterException, IOException, AlgorithmException {
        // first validate
        validate(inputDataSets, parameters);
        
        // retrieve input parameters and datasets
        ItemSimilarityMetric itemSimilarityMetric = ItemSimilarityMetric.valueOf(parameters.getProperty(ITEM_SIMILARITY_METRIC));
        DataSetEntity inputDataSet = inputDataSets.get(0);
        File file = inputDataSet.getDataFile();
        List<DataSetEntity> outputList = new ArrayList<DataSetEntity>(1);

        DataModel model = new FileDataModel(file);
        // get appropriate item similarity implementation
        // store item similarity in matrix
        double[][] matrix;
        long[] itemIdArray;
        try {
            ItemSimilarity similarity = getItemSimilarity(itemSimilarityMetric, model);
            if (similarity == null) {
                throw new AlgorithmException("Cannot find item similarity algorithm for [" + itemSimilarityMetric + "]");
            }
            
            LongPrimitiveIterator itemIds = model.getItemIDs();
            if (itemIds == null) {
                throw new AlgorithmException("Cannot find item ids for dataset [" + file + "]");
            }
            
            List<Long> itemIdList = new ArrayList<Long>();
            while (itemIds.hasNext()) {
                itemIdList.add(itemIds.nextLong());
            }

            matrix = new double[itemIdList.size()][itemIdList.size()];
            itemIdArray = new long[itemIdList.size()];
            for (int i = 0; i < itemIdArray.length; i++) {
                matrix[i][i] = 0;
                for (int j = 0; j < i; j++) {
                    double similarityValue = similarity.itemSimilarity(i+1, j+1);
                    matrix[i][j] = matrix[j][i] = similarityValue;
                }
            }
        } catch (TasteException e) {
            throw new AlgorithmException(e);
        }

        // create a temp file with the output
        File outputFile = new File("itemsimilaritymatrix." + inputDataSet.getFileSystemName() + "." + System.currentTimeMillis());
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        for (int i = 0; i < itemIdArray.length; i++) {
            for (int j = 0; j < i; j++) { // only need to write half the matrix
                bw.write(String.valueOf(matrix[i][j]));
                if (j < i - 1) { bw.write(","); }
            }
            bw.write("\n");
        }
        
        // now create output data set
        DataSetEntityBase output = new DataSetEntityBase();
        output.setName(outputFile.getName());
        output.setFileSystemName(outputFile.getName());
        output.setLocation(inputDataSet.getLocation());
        output.persist();
        output.putDataFile(outputFile); // push
        outputList.add(output);
        
        outputFile.delete(); // has been pushed
        return outputList;
    }

    private static ItemSimilarity getItemSimilarity(ItemSimilarityMetric itemSimilarityType, DataModel model)
            throws TasteException {
        switch (itemSimilarityType) {
        case LOG_LIKELIHOOD: return new LogLikelihoodSimilarity(model);
        case TANIMOTO_COEFFICIENT: new TanimotoCoefficientSimilarity(model);
        default: return null;
        }
    }

    /* (non-Javadoc)
     * @see io.algorithms.entity.Algorithm#getImplementationClass()
     */
    @Override
    public String getImplementationClass() {
        return "Algorithms_simItemNoPrefTrainer";
    }
}
