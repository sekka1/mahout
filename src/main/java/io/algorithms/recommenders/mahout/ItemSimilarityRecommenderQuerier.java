/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.recommenders.mahout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.algorithms.common.resource.Algorithm;
import io.algorithms.common.resource.AlgorithmException;
import io.algorithms.common.resource.InvalidDataSetException;
import io.algorithms.common.resource.InvalidParameterException;
import io.algorithms.entity.DataSetEntity;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.common.Parameters;

/**
 * Trains an item based recommender.
 */
public abstract class ItemSimilarityRecommenderQuerier implements Algorithm {
    private static final String NUM_REC = "numRec", ITEM_ID = "itemId";

    /* (non-Javadoc)
     * @see io.algorithms.common.resource.Algorithm#validate(java.util.List, org.apache.mahout.common.Parameters)
     */
    public List<DataSetEntity> validate(List<DataSetEntity> inputDataSets,
            Parameters parameters) throws InvalidDataSetException,
            InvalidParameterException {
        if (inputDataSets == null || inputDataSets.size() != 1) {
            throw new InvalidDataSetException("Missing Input Data");
        }
        if (parameters == null || parameters.get(NUM_REC) == null || parameters.get(ITEM_ID) == null) {
            throw new InvalidParameterException("Missing one or more required parameters [" + NUM_REC + ", " + ITEM_ID + "]");
        }
        
        try {
            Integer.parseInt(parameters.get(NUM_REC));
            Long.parseLong(parameters.get(ITEM_ID));
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(e);
        }

        // TODO: Return a real sample output dataset
        return inputDataSets;
    }

    /* (non-Javadoc)
     * @see io.algorithms.common.resource.Algorithm#run(java.util.List, org.apache.mahout.common.Parameters)
     */
    public List<DataSetEntity> run(List<DataSetEntity> inputDataSets, Parameters parameters)
            throws InvalidDataSetException, InvalidParameterException, IOException, AlgorithmException {
        validate(inputDataSets, parameters);
        long itemId = Long.parseLong(parameters.get(ITEM_ID));
        int numRec = Integer.parseInt(parameters.get(NUM_REC));
        DataSetEntity inputDataSet = inputDataSets.get(0);
        File f = inputDataSet.getDataFile();

        try {
            DataModel model = new GenericBooleanPrefDataModel(GenericBooleanPrefDataModel.toDataMap(new FileDataModel(f)));
            ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(model);
            ItemBasedRecommender recommender = new GenericBooleanPrefItemBasedRecommender(model, itemSimilarity);

        } catch (TasteException e) {
            throw new AlgorithmException(e);
        }
        
        return null;
    }
}
