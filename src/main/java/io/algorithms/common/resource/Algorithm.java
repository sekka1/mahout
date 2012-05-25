/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.common.resource;

import io.algorithms.entity.DataSetEntity;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Generic interface for all algorithms.
 */
public interface Algorithm {

    /**
     * Validates the supplied input datasets and parameters.
     * @param inputDataSets input data sets to be validated
     * @param parameters parameters to be validated
     * @throws InvalidDataSetException if any of the input datasets are invalid
     * @throws InvalidParameterException if any of the input parameters are invalid
     * @return List of sample output datasets for the given input datasets and parameters
     */
    List<DataSetEntity> validate(List<DataSetEntity> inputDataSets, Properties parameters) throws InvalidDataSetException, InvalidParameterException;
    
    /**
     * Runs the algorithm.
     * @param inputDataSets ids of the input datasets.
     * @param parameters runtime parameters
     * @throws InvalidDataSetException if any of the input datasets are invalid
     * @throws InvalidParameterException if any of the input parameters is invalid
     * @return output datasets.
     * @throws AlgorithmException 
     * @throws IOException 
     */
    List<DataSetEntity> run(List<DataSetEntity> inputDataSets, Properties parameters)
            throws InvalidDataSetException, InvalidParameterException, IOException, AlgorithmException;
}
