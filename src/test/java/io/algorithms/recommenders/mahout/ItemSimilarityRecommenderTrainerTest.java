/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.recommenders.mahout;

import java.util.Properties;

import io.algorithms.entity.DataSetEntity;
import io.algorithms.entity.DataSetEntityBase;
import junit.framework.TestCase;

/**
 * Tests item similarity based recommender trainers.
 */
public class ItemSimilarityRecommenderTrainerTest extends TestCase {
    private DataSetEntity inputDataSet;
    private Properties parameters;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        inputDataSet = new DataSetEntityBase();
    }

    public void testLogLikelihood() {
        
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
    }
}
