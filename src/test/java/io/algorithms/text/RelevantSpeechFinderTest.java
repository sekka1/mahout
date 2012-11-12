/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.text;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class RelevantSpeechFinderTest {
    RelevantSpeechFinder rsf;
    File file;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        rsf = new RelevantSpeechFinder();
        file = new File(ClassLoader.getSystemResource("famous_speeches.csv").toURI());
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
    @Test
    public void testFindRelevantSpeeches() throws Exception {
        Map<String, Map<String, Map<Double, Map<String, String>>>> results = rsf.findRelevantSpeeches(file, "necessary evil");
        assertNotNull(results);
        assertEquals(results.size(), 2);
        String inputWord = "necessary"; // first word
        assertTrue(results.keySet().contains(inputWord));
        Map<String, Map<Double, Map<String, String>>> resultsForInputWord = results.get(inputWord);
        assertNotNull(resultsForInputWord);
        assertTrue(resultsForInputWord.size() >= 16);
        String synonym = "indispensable";
        assertTrue(resultsForInputWord.keySet().contains(synonym));
        Map<Double, Map<String, String>> scoresForRelatedWord = resultsForInputWord.get(synonym);
        assertNotNull(scoresForRelatedWord);
        for (Double score : scoresForRelatedWord.keySet()) {
            Map<String, String> doc = scoresForRelatedWord.get(score);
            assertNotNull(doc);
            String speaker = doc.get("Speaker");
            assertNotNull(speaker);
            assertTrue(speaker.contains("Nixon"));
            break; // we just want to check the first entry
        }
    }

    /**
     * Test method for {@link io.algorithms.text.RelevantSpeechFinder#getRelatedWords(java.lang.String)}.
     */
    @Test
    public void testGetRelatedWords() throws IOException {
        String word = "tall";
        List<String> relatedWords = rsf.getRelatedWords(word);
        assertNotNull(relatedWords);
        assertTrue(relatedWords.size() >= 22);
        assertEquals(relatedWords.get(0), "tall");
        assertTrue(relatedWords.contains("gangly"));
    }

    /**
     * Test method for {@link io.algorithms.text.RelevantSpeechFinder#index(org.apache.lucene.analysis.Analyzer, java.io.File)} and
     * {@link io.algorithms.text.RelevantSpeechFinder#search(org.apache.lucene.analysis.Analyzer, org.apache.lucene.store.Directory, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testIndexAndSearch() throws Exception {
        Analyzer a = new StandardAnalyzer(RelevantSpeechFinder.LUCENE_CURRENT);
        Directory d = rsf.index(a, file);
        SortedMap<Double, Map<String, String>> results = rsf.search(a, d, "Transcript", "trouble");
        assertNotNull(results);
        assertEquals(results.size(), 4);
        assertEquals(results.firstKey().doubleValue(), 0.09, .01);
        assertEquals(results.get(results.firstKey()).get("Speaker"), "William Jefferson Clinton");
        assertEquals(results.lastKey().doubleValue(), 0.05, .01);
        assertEquals(results.get(results.lastKey()).get("Speaker"), "Martin Luther King, Jr.");
    }
}
