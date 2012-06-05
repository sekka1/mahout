/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.recommenders.mahout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.algorithms.common.resource.Algorithm;
import io.algorithms.common.resource.AlgorithmException;
import io.algorithms.common.resource.InvalidDataSetException;
import io.algorithms.common.resource.InvalidParameterException;
import io.algorithms.entity.DataSetEntity;
import io.algorithms.entity.DataSetEntityBase;

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
public class ItemSimilarityRecommenderTrainer {
    /**
     * Different item similarity metrics.
     */
    private static enum ItemSimilarityMetric {
        LOG_LIKELIHOOD,
        TANIMOTO_COEFFICIENT
    }

    private static final String ITEM_SIMILARITY_METRIC = "ItemSimilarityMetric";
    
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
//        File file = inputDataSet.getDataFile();
//        DataModel model = new FileDataModel(file);
//        try {
//            // get appropriate item similarity implementation
//            ItemSimilarity similarity = getItemSimilarity(itemSimilarityMetric, model);
//            if (similarity == null) {
//                throw new AlgorithmException("Cannot find item similarity algorithm for [" + itemSimilarityMetric + "]");
//            }
//            
//            LongPrimitiveIterator itemIds = model.getItemIDs();
//            if (itemIds == null) {
//                throw new AlgorithmException("Cannot find item ids for dataset id [" + inputDataSet.getId() + "]");
//            }
//            
//            List<Long> itemIdList = new ArrayList<Long>();
//            while (itemIds.hasNext()) {
//                itemIdList.add(itemIds.nextLong());
//            }
//
//            // store item similarity in matrix
//            final double[][] matrix = new double[itemIdList.size()][itemIdList.size()];
//            final long[] itemIdArray = new long[itemIdList.size()];
//            for (int i = 0; i < itemIdArray.length; i++) {
//                for (int j = i+1; j < itemIdArray.length; j++) {
//                    double similarityValue = similarity.itemSimilarity(i, j);
//                    matrix[i][j] = similarityValue;
//                    matrix[j][i] = similarityValue;
//                }
//            }
//
//            // write similarity matrix to output dataset
//            PipedInputStream pis = new PipedInputStream();
//            PipedOutputStream pos = new PipedOutputStream(pis);
//            final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(pos));
//            Callable task = new Callable() {
//                public Object call() throws Exception {
//                    for (int i = 0; i < itemIdArray.length; i++) {
//                        for (int j = 0; j < itemIdArray.length; j++) {
//                            bw.write(String.valueOf(matrix[i][j]));
//                        }
//                    }
//                    bw.close();
//                    return null;
//                }
//            };
//
//            DataSetEntity outputDataSet = new DataSetEntityBase();
//            outputDataSet.putDataStream(pis);
//
//            ExecutorService executor = Executors.newFixedThreadPool(1);
//            Future f = executor.submit(task);
//            try {
//                executor.awaitTermination(10, TimeUnit.MINUTES);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//
//            // TODO: need to persist outputDataSet
//
//            return f.isDone()? Arrays.asList(outputDataSet): null;
//
//        } catch (TasteException e) {
//            throw new AlgorithmException(e);
//        }
        return null;
    }

    private static ItemSimilarity getItemSimilarity(ItemSimilarityMetric itemSimilarityType, DataModel model)
            throws TasteException {
        switch (itemSimilarityType) {
        case LOG_LIKELIHOOD: return new LogLikelihoodSimilarity(model);
        case TANIMOTO_COEFFICIENT: new TanimotoCoefficientSimilarity(model);
        default: return null;
        }
    }
}
