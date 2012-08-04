/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.classification;

import io.algorithms.entity.Algorithm;
import io.algorithms.entity.AlgorithmException;
import io.algorithms.entity.DataSetEntity;
import io.algorithms.entity.InvalidDataSetException;
import io.algorithms.entity.InvalidParameterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Properties;

import org.apache.mahout.classifier.sgd.AdaptiveLogisticRegression;
import org.apache.mahout.classifier.sgd.RecordFactory;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;

import com.google.common.base.Preconditions;

/**
 * Implements logistic regression.
 */
public class LogisticRegressionClassifier implements Algorithm {
    private static final int NUM_FEATURES = 3;
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    
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
     * @param learner the learner to be trained
     * @param trainingData input stream with data for training the classifier.
     * The stream should be in CSV (comma separated) format. The first line should
     * be a header containing column names. Subsequent lines should have comma
     * separated fields, whose number should match that of the first line. The
     * Stream is not closed by this method and so the caller will have to do it.
     * @param factory recordFactory to be used to interpret input data
     * @throws IOException if something bad happens with IO
     */
    void trainBulk(final AdaptiveLogisticRegression learner, final InputStream trainingData, final RecordFactory factory)
            throws IOException {
        Preconditions.checkNotNull(learner); Preconditions.checkNotNull(trainingData); Preconditions.checkNotNull(factory);
        String line = null;
        boolean firstLine = true;
        BufferedReader trainingDataReader = new BufferedReader(new InputStreamReader(trainingData));
        while ((line = trainingDataReader.readLine()) != null) {
            System.out.println("input: " + line);
            if (firstLine) {
                factory.firstLine(line);
                firstLine = false;
            } else {
                Vector featureVector = new DenseVector(NUM_FEATURES);
                int clazz = factory.processLine(line, featureVector);
                 System.out.println("\t=> FeatureVector: " + featureVector + "\t=> Class: " + clazz);
                learner.train(clazz, featureVector);
            }
        }
        learner.close();
    }
    
    /**
     * Classifies the given vector, and also trains the underlying model.
     * @param learner AdaptiveLogisticRegression instance to be trained.
     * @param input input feature vector to be trained
     * @param factory recordFactory to be used to interpret input data
     */
    void train(final AdaptiveLogisticRegression learner, final String input, final RecordFactory factory) {
        Vector featureVector = new DenseVector(NUM_FEATURES);
        int clazz = factory.processLine(input, featureVector);
        learner.train(clazz, featureVector);
    }
    
    /**
     * Classifies the given vector, and also trains the underlying model.
     * @param learner AdaptiveLogisticRegression instance to be used for classification.
     * @param input input feature vector to be classified
     * @param factory recordFactory to be used to interpret input data
     * @return Vector of size numClasses-1 representing the probabilities of the classes 1 through numClasses.
     * Probability of the 0th class is 1-sum(rest).
     */
    double classify(final AdaptiveLogisticRegression learner, final String input, final RecordFactory factory) {
        Vector featureVector = new DenseVector(NUM_FEATURES);
        factory.processLine(input, featureVector);
        Matrix beta = learner.getBest().getPayload().getLearner().getModels().get(0).getBeta();
        StringBuilder betaString = new StringBuilder();
        for (int i = 0; i < beta.rowSize(); i++) {
            for (int j = 0; j < beta.columnSize(); j++) {
                betaString.append(FORMAT.format(beta.get(i, j))).append("\t");
            }
            betaString.append("\n");
        }
        double output = learner.getBest().getPayload().getLearner().classifyScalar(featureVector);
        System.out.println("input: " + input + "\n\t=> FeatureVector: " + featureVector + "\n"
                + betaString.toString()  + "\n\t=> Success: " + output);
        return output;
    }
}
