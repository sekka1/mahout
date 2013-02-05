/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
*/
package io.algorithms.text;

import io.algorithms.text.Lucene.ScoreDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.lucene.queryParser.ParseException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Finds speeches given  terms.
 * @author Rajiv
 */
public class RelevantSpeechFinder {
    
    private static final String DATASOURCE_KEY_TEXT = "text";
    
    private Lucene lucene;
    private File csvFile;
    
    public RelevantSpeechFinder() throws URISyntaxException, FileNotFoundException {
        csvFile = new File(Thread.currentThread().getContextClassLoader().getResource("famous_speeches.csv").toURI());
        lucene = new Lucene();
    }
    
    /**
     * Returns speeches that are relevant to a given search term.
     * @param speechFileCSV File that contains the speeches as a CSV file with the following columns:
     * Rank,Speaker,Title,Audio,Transcript
     * @param queryFileJSON query file which contains the
     * query text in a json structure {"text":"The input text"}
     * @return the search results as a map in the following format:
     *          foreach word w in input text
     *               for each word v related to w
     *                   list of documents in descending order of relevance to v
     *                   (each document is returned as a Map<FieldName, Value>
     *               end
     *            end
     * @throws IOException
     * @throws ParseException
     * @throws NoSuchAlgorithmException 
     */
    public Map<String, Map<String, List<ScoreDocument>>> findRelevantSpeeches(File speechFileCSV, File queryFileJSON) throws IOException, ParseException, NoSuchAlgorithmException {
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, String> json = mapper.readValue(queryFileJSON, Map.class);
        if (json != null && json.containsKey(DATASOURCE_KEY_TEXT)) {
            return findRelevantSpeeches(speechFileCSV, json.get(DATASOURCE_KEY_TEXT));
        }
        return null;
    }
    
    /**
     * Returns speeches that are relevant to a given search term.
     * @param speechFileCSV File that contains the speeches as a CSV file with the following columns:
     * Rank,Speaker,Title,Audio,Transcript
     * @param queryText The string of space separated words whose synomyms are to be searched across the speeches
     * @return the search results as a map in the following format:
     *          foreach word w in input text
     *               for each word v related to w
     *                   list of documents in descending order of relevance to v
     *                   (each document is returned as a Map<FieldName, Value>
     *               end
     *            end
     * @throws IOException
     * @throws org.apache.lucene.queryparser.classic.ParseException 
     * @throws ParseException
     * @throws org.apache.lucene.queryparser.classic.ParseException 
     */
    public Map<String, Map<String, List<ScoreDocument>>> findRelevantSpeeches(File speechFileCSV, String queryText) throws IOException, ParseException, NoSuchAlgorithmException {
        if (queryText == null) { return null; } 
        Map<String, Map<String, List<ScoreDocument>>> results = new LinkedHashMap<String, Map<String, List<ScoreDocument>>>();
        StringTokenizer st = new StringTokenizer(queryText);
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            if (word.length() < 4) continue; // reject small words
            word = word.toLowerCase();
            List<String> relatedWords = new Wordnet().getRelatedWords(word);
            Map<String, List<ScoreDocument>> partialResults = new LinkedHashMap<String, List<ScoreDocument>>();
            for (String relatedWord : relatedWords) {
                List<ScoreDocument> searchResults = lucene.searchCSV(csvFile, "Transcript", relatedWord);
                partialResults.put(relatedWord, searchResults);
            }
            results.put(word, partialResults);
        }
        return results;
    }
    
}
