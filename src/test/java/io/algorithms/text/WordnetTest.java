/*
* Copyright 2013 Algorithms.io. All Rights Reserved.
*/
package io.algorithms.text;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class WordnetTest {

    Wordnet wordnet;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        wordnet = new Wordnet();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link io.algorithms.text.RelevantSpeechFinder#getRelatedWords(java.lang.String)}.
     */
    @Test
    public void testGetRelatedWords() throws IOException {
        String word = "tall";
        List<String> relatedWords = wordnet.getRelatedWords(word);
        assertNotNull(relatedWords);
        assertTrue(relatedWords.size() >= 16);
        assertEquals(relatedWords.get(0), "tall");
        assertTrue(relatedWords.contains("gangly"));
    }

}
