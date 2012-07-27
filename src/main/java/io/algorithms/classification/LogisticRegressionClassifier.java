/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.classification;

import io.algorithms.entity.Algorithm;
import io.algorithms.entity.AlgorithmException;
import io.algorithms.entity.DataSetEntity;
import io.algorithms.entity.InvalidDataSetException;
import io.algorithms.entity.InvalidParameterException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.mahout.classifier.sgd.AdaptiveLogisticRegression;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.FeatureVectorEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

/**
 * Implements logistic regression.
 */
public class LogisticRegressionClassifier implements Algorithm {

    /* (non-Javadoc)
     * @see io.algorithms.entity.Algorithm#getImplementationClass()
     */
    @Override
    public String getImplementationClass() {
        return "classification_logistic_regression";
    }

    /* (non-Javadoc)
     * @see io.algorithms.entity.Algorithm#validate(java.util.List, java.util.Properties)
     */
    @Override
    public List<DataSetEntity> validate(List<DataSetEntity> inputDataSets,
            Properties parameters) throws InvalidDataSetException,
            InvalidParameterException, IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see io.algorithms.entity.Algorithm#run(java.util.List, java.util.Properties)
     */
    @Override
    public List<DataSetEntity> run(List<DataSetEntity> inputDataSets,
            Properties parameters) throws InvalidDataSetException,
            InvalidParameterException, IOException, AlgorithmException {
        return validate(inputDataSets, parameters);
    }

    /**
     * Train the logistic regression classifier with an input file
     * @param input input file for training logistic regression classifier.
     * The file should be in the following format:
     * 
     * @return The name of the file containing trained parameters
     * @throws IOException if something bad happens with IO
     */
    public String train(BufferedReader in, String targetColumnName, Map<String, String> columnNameToTypeMap) throws IOException {

        Vector v = new RandomAccessSparseVector();
        String line = null;
        while ((line = in.readLine()) != null) {
            
        }
        
        AdaptiveLogisticRegression learner = new AdaptiveLogisticRegression();
        FeatureVectorEncoder encoder = new StaticWordValueEncoder("word_encoder");

        encoder.addToVector(word, v);
        learner.train();
        
        // output trained parameters to file and return it
        File output = new File(input + ".train." + System.currentTimeMillis());
        learner.write(new RandomAccessFile(output, "w"));
        return output.getName();
    }
    
}
