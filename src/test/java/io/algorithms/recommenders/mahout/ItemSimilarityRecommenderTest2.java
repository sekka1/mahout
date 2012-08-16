/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package test.java.io.algorithms.recommenders.mahout;

import static org.junit.Assert.*;
import io.algorithms.clustering.mahout.SimClusteringParallel;
import io.algorithms.recommenders.mahout.SimItemCityBlock;
import io.algorithms.recommenders.mahout.SimItemCityBlockEvaluator;
import io.algorithms.recommenders.mahout.SimItemEuclideanDistance;
import io.algorithms.recommenders.mahout.SimItemEuclideanDistanceEvaluator;
import io.algorithms.recommenders.mahout.SimItemLogLikelihood;
import io.algorithms.recommenders.mahout.SimItemLogLikelihoodNoPref;
import io.algorithms.recommenders.mahout.SimItemLogLikelihoodNoPrefEvaluator;
import io.algorithms.recommenders.mahout.SimItemTanimotoCoefficient;
import io.algorithms.recommenders.mahout.SimItemUncenteredCosine;
import io.algorithms.recommenders.mahout.SimItemUncenteredCosineEvaluator;
import io.algorithms.recommenders.mahout.SimUserLogLikelihood;
import io.algorithms.recommenders.mahout.SimUserLogLikelihoodNoPref;
import io.algorithms.recommenders.mahout.SimUserLogLikelihoodNoPrefEvaluator;
import io.algorithms.recommenders.mahout.SimUserPearsonCorrelation;
import io.algorithms.recommenders.mahout.SimUserPearsonCorrelationEvaluator;
import io.algorithms.recommenders.mahout.SimUserSpearmanCorrelation;
import io.algorithms.recommenders.mahout.SimUserSpearmanCorrelationEvaluator;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Tests item similarity based recommender trainers.
 */
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@Configurable
public class ItemSimilarityRecommenderTest2 extends AbstractTransactionalJUnit4SpringContextTests {
    private static final String FOLDER = "src/test/resources", INPUT_FILE = "rec_1075", CLUSTER_FILE = "clusterPoints.txt";
    
    static {
    }
    
    @Test
    public void tryCluster()
    {
    	// Run Action
    	SimClusteringParallel simClusteringParallel = new SimClusteringParallel();
        String outputString = simClusteringParallel.get(CLUSTER_FILE,"3");
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    
    
   
    @Test
    public void tryItemCityBlock()
    {
        // Run Action
    	SimItemCityBlock testCase = new SimItemCityBlock();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }

    @Test
    public void tryItemCityBlockEvaluator()
    {
        // Run Action
    	SimItemCityBlockEvaluator testCase = new SimItemCityBlockEvaluator();
        String outputString = testCase.get( INPUT_FILE, "2408", "15", "10" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryItemEuclideanDistance()
    {
        // Run Action
    	SimItemEuclideanDistance testCase = new SimItemEuclideanDistance();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryItemEuclideanDistanceEvaluator()
    {
        // Run Action
    	SimItemEuclideanDistanceEvaluator testCase = new SimItemEuclideanDistanceEvaluator();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryItemUncenteredCosine()
    {
        // Run Action
    	SimItemUncenteredCosine testCase = new SimItemUncenteredCosine();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryItemUncenteredCosineEvaluator()
    {
        // Run Action
    	SimItemUncenteredCosineEvaluator testCase = new SimItemUncenteredCosineEvaluator();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryItemTanimotoCoefficient()
    {
        // Run Action
    	SimItemTanimotoCoefficient testCase = new SimItemTanimotoCoefficient();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryItemLogLikelihood()
    {
        // Run Action
    	SimItemLogLikelihood testCase = new SimItemLogLikelihood();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryLogLikelihoodNoPref()
    {
        // Run Action
        SimItemLogLikelihoodNoPref testCase = new SimItemLogLikelihoodNoPref();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryLogLikelihoodNoPrefEvaluator()
    {
        // Run Action
        SimItemLogLikelihoodNoPrefEvaluator testCase = new SimItemLogLikelihoodNoPrefEvaluator();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryUserLogLikelihood()
    {
        // Run Action
    	SimUserLogLikelihood testCase = new SimUserLogLikelihood();
        String outputString = testCase.get( INPUT_FILE, "2408", "15", "10" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    //NOTE: THIS USES A FILE CALLED intro.csv and not the parameter!!!
    @Test
    public void tryPearonCorrelation()
    {
        // Run Action
        SimUserPearsonCorrelation testCase = new SimUserPearsonCorrelation();
        String outputString = testCase.get( INPUT_FILE, "1", "1", "2" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void trySpearmanCorrelation()
    {
        // Run Action
        SimUserSpearmanCorrelation testCase = new SimUserSpearmanCorrelation();
        String outputString = testCase.get( INPUT_FILE, "1", "1", "2" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void trySpearmanCorrelationEvaluator()
    {
        // Run Action
        SimUserSpearmanCorrelationEvaluator testCase = new SimUserSpearmanCorrelationEvaluator();
        String outputString = testCase.get( INPUT_FILE, "1", "1" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    
    @Test
    public void tryPearonCorrelationEvaluator()
    {
        // Run Action
    	SimUserPearsonCorrelationEvaluator testCase = new SimUserPearsonCorrelationEvaluator();
        String outputString = testCase.get( INPUT_FILE, "2408", "15" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryUserLogLikelihoodNoPref()
    {
        // Run Action
    	SimUserLogLikelihoodNoPref testCase = new SimUserLogLikelihoodNoPref();
        String outputString = testCase.get( INPUT_FILE, "2408", "15", "10" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    @Test
    public void tryUserLogLikelihoodNoPrefEvaluator()
    {
        // Run Action
    	SimUserLogLikelihoodNoPrefEvaluator testCase = new SimUserLogLikelihoodNoPrefEvaluator();
        String outputString = testCase.get( INPUT_FILE, "2408", "15", "10" );
        // Output results
        System.out.println( outputString );
        assertFalse(outputString.contains("err") || outputString.contains("fail") || outputString.length() < 3);
    }
    
    
    
}