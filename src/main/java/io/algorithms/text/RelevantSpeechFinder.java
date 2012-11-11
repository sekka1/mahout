/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.text;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import com.googlecode.jcsv.reader.internal.DefaultCSVEntryParser;

/**
 * Searches an index for a term..
 */
public class RelevantSpeechFinder {
    static final Version LUCENE_CURRENT = Version.LUCENE_40;

    public void findRelevantSpeeches(File csvFile, String searchField, String word) throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer(LUCENE_CURRENT);
        Directory index = index(analyzer, csvFile);
        List<String> relatedWords = getRelatedWords(word);
        Map<String, List<ScoreDocument>> results = new HashMap<String, List<ScoreDocument>>();
        for (String relatedWord : relatedWords) {
            List<ScoreDocument> searchResults = search(analyzer, index, searchField, relatedWord);
            results.put(relatedWord, searchResults);
        }
//        return result;
    }
    
    /**
     * Returns a list of words related to the given word.
     * @param word
     * @return
     */
    private List<String> getRelatedWords(String word) {
        // for now we will just return the word as is.
        // hook this up to wordnet
        return new ArrayList<String>(Arrays.asList(word));
    }
    
    /**
     * This method indexes a set of documents encoded in a CSV file.
     * The columns in the CSV file indicate various attributes of the document
     * (including one that contains the actual contents). All of the attributes
     * are added to the index.
     * @param analyzer analyzer to use
     * @param csvFile documents formatted as above
     * @return the index
     * @throws IOException
     */
    private Directory index(Analyzer analyzer, File csvFile) throws IOException {
        File index = new File("lucene-index-" + System.currentTimeMillis());
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_CURRENT, analyzer);
        Directory directory = null;
        IndexWriter iwriter = null;
        try {
            directory = FSDirectory.open(index);
            iwriter = new IndexWriter(directory, config);
            CSVReader<String[]> reader = new CSVReaderBuilder<String[]>(new FileReader(csvFile)).strategy(CSVStrategy.DEFAULT).entryParser(new DefaultCSVEntryParser()).build();
            List<String> header = reader.readHeader();
            if (header != null) {
                String[] line;
                while ((line = reader.readNext()) != null) {
                    Document doc = new Document();
                    for (int i = 0; i < header.size(); i++) {
                        doc.add(new Field(header.get(i), line[i], TextField.TYPE_STORED));
                    }
                    iwriter.addDocument(doc);
                }
            }
        } finally {
            if (iwriter != null) iwriter.close();
        }
        return directory;
    }

    /**
     * Returns search results as a list of ScoreDocument objects/
     * @param analyzer analyzer to use
     * @param index Index to search
     * @param searchField default field within document that the search term should be applied to
     * @param searchTerm string to be searched
     * @return list of documents that match along with their score
     * @throws IOException
     * @throws ParseException
     */
    private List<ScoreDocument> search(Analyzer analyzer, Directory index, String searchField, String searchTerm) throws IOException, ParseException {
        IndexReader ireader = null;
        List<ScoreDocument> results = new ArrayList<ScoreDocument>();
        try {
            // Now search the index:
            ireader = DirectoryReader.open(index);
            IndexSearcher isearcher = new IndexSearcher(ireader);
            // Parse a simple query that searches for "text":
            QueryParser parser = new QueryParser(LUCENE_CURRENT, searchField, analyzer);
            Query query = parser.parse(searchTerm);
            ScoreDoc[] hits = isearcher.search(query, null, 10).scoreDocs;
            if (hits == null) return null;
            for (int i = 0; i < hits.length; i++) {
               results.add(new ScoreDocument(hits[i].score, isearcher.doc(hits[i].doc))); 
            }
        } finally {
            ireader.close();
        }
        return results;
    }
    
    private static final class ScoreDocument {
        final double score;
        final Document document;
        ScoreDocument(double score, Document document) {
            this.score = score;
            this.document = document;
        }
    }
}

