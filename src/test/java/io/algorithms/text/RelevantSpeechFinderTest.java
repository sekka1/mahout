/*
* Copyright 2001-2012 ArcSight, Inc. All Rights Reserved.
*
* This software is the proprietary information of ArcSight, Inc.
* Use is subject to license terms.
*
* $Author: rajiv$
* $Date: Nov 11, 2012$
*/
package io.algorithms.text;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Map;

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

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        rsf = new RelevantSpeechFinder();
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
    public void testFindRelevantSpeeches() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link io.algorithms.text.RelevantSpeechFinder#getRelatedWords(java.lang.String)}.
     */
    @Test
    public void testGetRelatedWords() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link io.algorithms.text.RelevantSpeechFinder#index(org.apache.lucene.analysis.Analyzer, java.io.File)} and
     * {@link io.algorithms.text.RelevantSpeechFinder#search(org.apache.lucene.analysis.Analyzer, org.apache.lucene.store.Directory, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testIndexAndSearch() throws Exception {
        Analyzer a = new StandardAnalyzer(RelevantSpeechFinder.LUCENE_CURRENT);
        URL url = ClassLoader.getSystemResource("famous_speeches.csv");
        Directory d = rsf.index(a, new File(url.toURI()));
        Map<Double, Map<String, String>> results = rsf.search(a, d, "Transcript", "dream");
        for (Double score : results.keySet()) {
            System.out.print(score + ":" + results.get(score).get("Title"));
        }
    }
}
