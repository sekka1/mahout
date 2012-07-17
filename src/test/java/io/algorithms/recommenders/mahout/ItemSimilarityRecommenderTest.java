/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.recommenders.mahout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import io.algorithms.datastore.AmazonDataStore;
import io.algorithms.datastore.DataStore;
import io.algorithms.datastore.DataStoreDuplicateException;
import io.algorithms.datastore.DataStoreException;
import io.algorithms.datastore.DataStoreRegister;
import io.algorithms.datastore.FileDataStore;
import io.algorithms.entity.Algorithm;
import io.algorithms.entity.AlgorithmDuplicateException;
import io.algorithms.entity.AlgorithmException;
import io.algorithms.entity.AlgorithmRegister;
import io.algorithms.entity.DataSetEntity;
import io.algorithms.entity.DataSetEntityBase;
import org.junit.Test;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * Tests item similarity based recommender trainers.
 */
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@Configurable
public class ItemSimilarityRecommenderTest extends AbstractTransactionalJUnit4SpringContextTests {
    static {
        // TODO: This is test code. Please remove!
        DataStore s3dataStore = new AmazonDataStore(new AmazonS3Client(new BasicAWSCredentials("AKIAJO6OOIFG3LCMZPGA", "sQNUF++7eFhh8JIlTNgUnKKx3HdOhRmN+V7pto5F")));
        try {
            DataStoreRegister.registerDataStore("S3", s3dataStore);
            DataStoreRegister.registerDataStore("File", new FileDataStore());
        } catch (DataStoreDuplicateException e1) {
            e1.printStackTrace();
        } catch (DataStoreException e1) {
            e1.printStackTrace();
        }
        
        Algorithm itemSimilarityRecommender = new ItemSimilarityRecommenderTrainer();
        try {
            AlgorithmRegister.registerAlgorithm(itemSimilarityRecommender.getImplementationClass(), itemSimilarityRecommender);
        } catch (AlgorithmDuplicateException e) {
            e.printStackTrace();
        } catch (AlgorithmException e) {
            e.printStackTrace();
        }
        
    }
    
    @Test
    public void testLogLikelihood() throws Exception {
        String folderName = "src/test/resources", fileName = "rec_1908";
        
        // delete previous output files
        // for whatever reason, these seem to be confusing the input
        File folder = new File(folderName);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (!file.getName().equals(fileName)) {
                file.delete();
            }
        }
        
        List<DataSetEntity> inputDataSets = new ArrayList<DataSetEntity>(1);
        DataSetEntityBase inputDataSet = new DataSetEntityBase();
        inputDataSet.setLocation("File," + folderName);
        inputDataSet.setFileSystemName(fileName);
        inputDataSet.persist();
        inputDataSets.add(inputDataSet);
        
        Algorithm simItemLogLikelihood = new ItemSimilarityRecommenderTrainer();
        Properties parameters = new Properties();
        parameters.setProperty(ItemSimilarityRecommenderTrainer.ITEM_SIMILARITY_METRIC, ItemSimilarityRecommenderTrainer.ItemSimilarityMetric.LOG_LIKELIHOOD.toString());
        
        // first validate
        List<DataSetEntity> outputs = simItemLogLikelihood.validate(inputDataSets, parameters); // throws IOException
        assert outputs != null;
        // TODO: add additional assertions regarding outputs
        
        // now run it
        long startTime = System.currentTimeMillis();
        outputs = simItemLogLikelihood.run(inputDataSets, parameters);
        assert outputs != null;
        assert outputs.size() == 1;
        DataSetEntityBase output = (DataSetEntityBase) outputs.get(0);
        assert output != null;
        String fileSystemName = output.getFileSystemName();
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Output saved to [" + fileSystemName + "], took [" + duration + "] ms");

        // remove from database
        inputDataSet.remove();
        output.remove();
        inputDataSets = null;
    }
}
