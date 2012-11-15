/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.classification;

import static org.junit.Assert.*;

import io.algorithms.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mahout.classifier.sgd.CrossFoldLearner;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.classifier.sgd.RecordFactory;
import org.apache.mahout.math.Vector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Tests logistic regression classifier;
 */
public class LogisticRegressionClassifierTest {
    private static final String FOLDER = "src/test/resources", INPUT_FILE = "quote_5.csv";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testClassifier() throws Exception {
        LogisticRegressionClassifier classifier = new LogisticRegressionClassifier();
        String targetColumnName = "CONVERTED_TO_ORDER", idColumnName = "OID";
        Map<String, String> columnNameToTypeMap = new HashMap<String, String>();
        columnNameToTypeMap.put("TECHNOLOGY", "word");
//        columnNameToTypeMap.put(idColumnName, "numeric");
        columnNameToTypeMap.put("MPN", "word");
        columnNameToTypeMap.put("CUSTOMER_NAME", "word");
        columnNameToTypeMap.put("DISTRIBUTOR", "word");
        columnNameToTypeMap.put("END_CUSTOMER_NAME", "word");
        columnNameToTypeMap.put("PROGRAM_NAME", "word");
        columnNameToTypeMap.put("APPLICATION_NAME", "word");
        columnNameToTypeMap.put("QUANTITY", "numeric");
        columnNameToTypeMap.put("OEM_PRICE", "numeric");
//        columnNameToTypeMap.put("DISTI_COST", "numeric");
//        columnNameToTypeMap.put("DISTI_RESALE", "numeric");
//        columnNameToTypeMap.put("TARGET_PRICE", "numeric");
        columnNameToTypeMap.put(targetColumnName, "word");
        List<String> targetClasses = Lists.newArrayList("N", "Y");
        RecordFactory factory = IOUtils.createRecordFactoryForInputFormat(columnNameToTypeMap, targetColumnName, targetClasses);

//        for (double j = 0.5; j <=4; j+=0.5) {
//            System.out.println("Will try j = " + 1);
//            InputStream trainingData = new FileInputStream(new File(FOLDER, INPUT_FILE));
//            OnlineLogisticRegression learner = new OnlineLogisticRegression(2, LogisticRegressionClassifier.NUM_FEATURES, new L1());
//            learner.lambda(0.01);
//            learner.learningRate(1);
//            classifier.trainBulk(learner, trainingData, factory);
            
//            Vector output = null;
//            for (double i = 0; i < 5; i += 1) {
//                String input = "\"XTL MTL AT/BT\",\"FLEXTRONICS GERMANY\",\"B&O\",\"ct\",\"100,000\"," + i + ",N";
//                output = classifier.classify(learner, input, factory);
//                System.out.println("Price: " + i + "\tProbability: " + output.get(1));
//            }
//        }
//        assertTrue("Expected success", output.get(1) < 0.5);
    }
}
