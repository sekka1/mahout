/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.text;

import static org.junit.Assert.*;

import io.algorithms.text.RelevantSpeechFinder.ScoreDocument;

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
    RelevantSpeechFinder rsf;
    InputStream file;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        rsf = new RelevantSpeechFinder();
        file = ClassLoader.getSystemResourceAsStream("famous_speeches.csv");
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
        Map<String, Map<String, List<ScoreDocument>>> results = rsf.findRelevantSpeeches("necessary evil");
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

    /**
     * Test method for {@link io.algorithms.text.RelevantSpeechFinder#getRelatedWords(java.lang.String)}.
     */
    @Test
    public void testGetRelatedWords() throws IOException {
        String word = "tall";
        List<String> relatedWords = rsf.getRelatedWords(word);
        assertNotNull(relatedWords);
        assertTrue(relatedWords.size() >= 16);
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
        List<ScoreDocument> results = rsf.search(a, d, "Transcript", "trouble");
        assertNotNull(results);
        assertEquals(results.size(), 7);
        assertEquals(results.get(2).score, 0.069, .01);
        assertEquals(results.get(2).document.get("Title"), "Cambodian Incursion Address");
        assertEquals(results.get(6).score, 0.046, .01);
        assertEquals(results.get(6).document.get("Speaker"), "Martin Luther King, Jr.");
    }
}
