/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.recommenders.mahout;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.test.context.ContextConfiguration;

import io.algorithms.entity.Algorithm;
import io.algorithms.entity.DataSetEntity;
import io.algorithms.entity.DataSetEntityBase;
import junit.framework.TestCase;

/**
 * Tests item similarity based recommender trainers.
 */
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
public class ItemSimilarityRecommenderTrainerTest extends TestCase {
    private List<DataSetEntity> inputDataSets;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        if (inputDataSets == null) {
            inputDataSets = new ArrayList<DataSetEntity>(1);
            DataSetEntityBase inputDataSet = new DataSetEntityBase();
            inputDataSet.setLocation("File,src/test/resources/rec_1908");
            inputDataSet.setName("rec_1908");
            inputDataSet.persist();
            inputDataSets.add(inputDataSet);
        }
    }

    public void testLogLikelihood() throws Exception {
        Algorithm simItemLogLikelihood = new ItemSimilarityRecommenderTrainer();
        Properties parameters = new Properties();
        parameters.setProperty(ItemSimilarityRecommenderTrainer.ITEM_SIMILARITY_METRIC, ItemSimilarityRecommenderTrainer.ItemSimilarityMetric.LOG_LIKELIHOOD.toString());
        
        // first validate
        List<DataSetEntity> outputs = simItemLogLikelihood.validate(inputDataSets, parameters); // throws IOException
        assertNotNull(outputs);
        // TODO: add additional assertions regarding outputs
        
        // now run it
        outputs = simItemLogLikelihood.run(inputDataSets, parameters);
        assertNotNull(outputs);
        assertEquals(outputs.size(), 1);
        DataSetEntity output = outputs.get(0);
        assertNotNull(output);
        String fileSystemName = output.getFileSystemName();
        System.out.println("Output saved to [" + fileSystemName + "]");
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        if (inputDataSets != null && inputDataSets.size() > 0) {
            DataSetEntityBase inputDataSet = (DataSetEntityBase) inputDataSets.get(0);
            inputDataSet.remove();
            inputDataSet = null;
            inputDataSets = null;
        }
    }
}
