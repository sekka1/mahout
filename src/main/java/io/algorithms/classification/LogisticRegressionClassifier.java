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
import io.algorithms.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.mahout.classifier.AbstractVectorClassifier;
import org.apache.mahout.classifier.OnlineLearner;
import org.apache.mahout.classifier.sgd.CsvRecordFactory;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.classifier.sgd.RecordFactory;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;

import com.google.common.base.Preconditions;

/**
 * Implements logistic regression.
 */
public class LogisticRegressionClassifier implements Algorithm {
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

    public String trainAndClassify(File inputFile, Map<String, String> columnNameToTypeMap,
            String targetColumnName, List<String> targetClasses, String query) throws IOException {

        Preconditions.checkNotNull(columnNameToTypeMap);
        Preconditions.checkNotNull(targetColumnName);
        Preconditions.checkNotNull(targetClasses);
        Preconditions.checkNotNull(query);
        
        CsvRecordFactory factory = IOUtils.createRecordFactoryForInputFormat(columnNameToTypeMap, targetColumnName, targetClasses);
        int numFeatures = columnNameToTypeMap.size() * 10 > 1000 ? 1000 : columnNameToTypeMap.size() * 10 ;
        OnlineLogisticRegression learner = new OnlineLogisticRegression(2, numFeatures, new L1());
        learner.lambda(0.1);
        learner.learningRate(1);
        InputStream trainingData = new FileInputStream(inputFile);
        trainBulk(trainingData, learner, factory, numFeatures);
        Vector output = classify(query, learner, factory, numFeatures);
        int maxIndex = output.maxValueIndex();
        return targetClasses.get(maxIndex);
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
    void trainBulk(final InputStream trainingData, final OnlineLearner learner, final RecordFactory factory, int numFeatures)
            throws IOException {
        Preconditions.checkNotNull(learner); Preconditions.checkNotNull(trainingData); Preconditions.checkNotNull(factory);
        String line = null;
        boolean firstLine = true;
        BufferedReader trainingDataReader = new BufferedReader(new InputStreamReader(trainingData));
        List<String> lines = new ArrayList<String>();
        while ((line = trainingDataReader.readLine()) != null) {
            if (firstLine) {
                factory.firstLine(line);
                firstLine = false;
            } else {
                lines.add(line);
            }
        }
        
        int numRepeats = lines.size() > 1E7 ? 1 : (int) Math.floor(1E7 / lines.size());
        for (int i = 0; i < numRepeats; i++) {
            Collections.shuffle(lines);
            for (String l1ne : lines) {
                train(l1ne, learner, factory, numFeatures);
            }
        }
        learner.close();
        Matrix beta = ((OnlineLogisticRegression) learner).getBeta();
        printMatrix(beta);
    }

    /**
     * Classifies the given vector, and also trains the underlying model.
     * @param learner CrossfoldLearner instance to be trained.
     * @param input input feature vector to be trained
     * @param factory recordFactory to be used to interpret input data
     */
    void train(final String input, final OnlineLearner learner, final RecordFactory factory, int numFeatures) {
        Vector featureVector = new DenseVector(numFeatures);
        int clazz = factory.processLine(input, featureVector);
        learner.train(clazz, featureVector);
    }

    static void printMatrix(Matrix beta) {
        StringBuilder betaString = new StringBuilder("TrainedParameters => ");
        for (int i = 0; i < beta.rowSize(); i++) {
            for (int j = 0; j < beta.columnSize(); j++) {
                betaString.append(FORMAT.format(beta.get(i, j))).append("\t");
            }
            betaString.append("\n");
        }
        System.out.println(betaString.toString());
    }
    

    /**
     * Classifies the given vector, and also trains the underlying model.
     * @param learner CrossfoldLearner instance to be used for classification.
     * @param input input feature vector to be classified
     * @param factory recordFactory to be used to interpret input data
     * @return Vector of size numClasses-1 representing the probabilities of the classes 1 through numClasses.
     * Probability of the 0th class is 1-sum(rest).
     */
    Vector classify(final String input, final AbstractVectorClassifier learner, final RecordFactory factory, int numFeatures) {
        Vector featureVector = new DenseVector(numFeatures);
        factory.processLine(input, featureVector);
        Vector output = learner.classifyFull(featureVector);
        System.out.println("input: " + input + "\n\t=> FeatureVector: " + featureVector + "\n\t=> Success: " + FORMAT.format(output.get(1)));
        
        return output;
    }
}
