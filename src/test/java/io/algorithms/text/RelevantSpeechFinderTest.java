/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.text;

import static org.junit.Assert.*;

import io.algorithms.text.Lucene.ScoreDocument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests all the methods in RelevantSpeechFinder.
 */
public class RelevantSpeechFinderTest {
    File csvFile;
    RelevantSpeechFinder rsf;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        rsf = new RelevantSpeechFinder();
        csvFile = new File(ClassLoader.getSystemResource("famous_speeches.csv").toURI());
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        rsf = null;
    }

    /**
     * Test method for {@link io.algorithms.text.RelevantSpeechFinder#findRelevantSpeeches(java.io.File, java.lang.String)}.
     */
    // @Test // This won't work on hosts that don't have wordnet
    public void testFindRelevantSpeeches() throws Exception {
        Map<String, Map<String, List<ScoreDocument>>> results = rsf.findRelevantSpeeches(csvFile, "necessary evil");
        assertNotNull(results);
        assertEquals(results.size(), 2);
        String inputWord = "necessary"; // first word
        assertTrue(results.keySet().contains(inputWord));
        Map<String, List<ScoreDocument>> resultsForInputWord = results.get(inputWord);
        assertNotNull(resultsForInputWord);
        assertTrue(resultsForInputWord.size() >= 10);
        String synonym = "essential";
        assertTrue(resultsForInputWord.keySet().contains(synonym));
        List<ScoreDocument> scoresForRelatedWord = resultsForInputWord.get(synonym);
        assertNotNull(scoresForRelatedWord);
        assertEquals(scoresForRelatedWord.size(), 10);
        assertEquals(scoresForRelatedWord.get(2).score, 0.0927, 0.1);
        Map<String, String> doc = scoresForRelatedWord.get(2).document;
        assertNotNull(doc);
        String speaker = doc.get("Speaker");
        assertNotNull(speaker);
        assertTrue(speaker.contains("Marshall"));
    }
}
