/*
* Copyright 2013 Algorithms.io. All Rights Reserved.
*/
package io.algorithms.text;

import static org.junit.Assert.*;

import io.algorithms.text.Lucene.ScoreDocument;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class LuceneTest {

    File csvFile, zipFile;
    Lucene lucene;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        csvFile = new File(ClassLoader.getSystemResource("famous_speeches.csv").toURI());
        zipFile = new File(ClassLoader.getSystemResource("famous_speeches.zip").toURI());
        lucene = new Lucene();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }


    /**
     * Test method for {@link io.algorithms.text.RelevantSpeechFinder#index(org.apache.lucene.analysis.Analyzer, java.io.File)} and
     * {@link io.algorithms.text.RelevantSpeechFinder#search(org.apache.lucene.analysis.Analyzer, org.apache.lucene.store.Directory, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testSearchCSV() throws Exception {
        List<ScoreDocument> results = lucene.searchCSV(csvFile, "Transcript", "trouble");
        assertNotNull(results);
        assertEquals(results.size(), 7);
        assertEquals(results.get(2).score, 0.069, .01);
        assertEquals(results.get(2).document.get("Title"), "Cambodian Incursion Address");
        assertEquals(results.get(6).score, 0.046, .01);
        assertEquals(results.get(6).document.get("Speaker"), "Martin Luther King, Jr.");
    }

    /**
     * Test method for {@link io.algorithms.text.RelevantSpeechFinder#index(org.apache.lucene.analysis.Analyzer, java.io.File)} and
     * {@link io.algorithms.text.RelevantSpeechFinder#search(org.apache.lucene.analysis.Analyzer, org.apache.lucene.store.Directory, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testSearchZip() throws Exception {
        List<ScoreDocument> results = lucene.searchZip(zipFile, null, "dream");
        assertNotNull(results);
        assertEquals(results.size(), 1);
        assertEquals(results.get(0).score, 0.204, .01);
        assertEquals(results.get(0).document.get(Lucene.FIELD_NAME), "famous_speeches/MLKDream.txt");
    }
}
