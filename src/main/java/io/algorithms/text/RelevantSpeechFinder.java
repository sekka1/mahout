/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
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

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.morph.WordnetStemmer;

/**
 * Finds speeches given search terms.
 */
public class RelevantSpeechFinder {
    
    static final Version LUCENE_CURRENT = Version.LUCENE_40;
    static final File INDEX_FOLDER = new File("tmp");
    static final String INDEX_FILE_PREFIX = "lucene-index-";

    private InputStream csvFile;
    
    public RelevantSpeechFinder() throws URISyntaxException, FileNotFoundException {
        csvFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("famous_speeches.csv");
    }
    
    /**
     * Returns speeches that are relevant to a given search term.
     * @param csvFile CSV file containing all the speeches. It must have the following columns: Rank,Speaker,Title,Audio,Transcript
     * @param word The word whose synomyms are to be searched across the speeches
     * @return the search results as a map in the following format:
     *          foreach word w in input text
     *               for each word v related to w
     *                   list of documents in descending order of relevance to v
     *                   (each document is returned as a Map<FieldName, Value>
     *               end
     *            end
     * @throws IOException
     * @throws ParseException
     */
    public Map<String, Map<String, List<ScoreDocument>>> findRelevantSpeeches(String queryText) throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer(LUCENE_CURRENT);
        Map<String, Map<String, List<ScoreDocument>>> results = new LinkedHashMap<String, Map<String, List<ScoreDocument>>>();
        Directory index = null;
        try {
            index = index(analyzer, csvFile);
            StringTokenizer st = new StringTokenizer(queryText);
            while (st.hasMoreTokens()) {
                String word = st.nextToken();
                if (word.length() < 4) continue; // reject small words
                word = word.toLowerCase();
                List<String> relatedWords = getRelatedWords(word);
                Map<String, List<ScoreDocument>> partialResults = new LinkedHashMap<String, List<ScoreDocument>>();
                for (String relatedWord : relatedWords) {
                    List<ScoreDocument> searchResults = search(analyzer, index, "Transcript", relatedWord);
                    partialResults.put(relatedWord, searchResults);
                }
                results.put(word, partialResults);
            }
        } finally {
            if (index != null) index.close();
        }
        return results;
    }
    
    /**
     * Returns a list of words related to the given word. This includes synonyms and hypernyms.
     * @param word
     * @return
     */
    List<String> getRelatedWords(String word) throws IOException {
        List<String> result = new ArrayList<String>();
        IDictionary dict = new Dictionary(new File("/usr/share/wordnet"));
        try {
            dict.open();
            IStemmer stemmer = new WordnetStemmer(dict);
            for (POS pos : EnumSet.of(POS.ADJECTIVE, POS.ADVERB, POS.NOUN, POS.VERB)) {
                List<String> resultForPos = new ArrayList<String>();
                List<String> stems = new ArrayList<String>();
                stems.add(word);
                for (String stem : stemmer.findStems(word,  pos)) {
                    if (!stems.contains(stem))
                        stems.add(stem);
                }
                for (String stem : stems) {
                    if (!resultForPos.contains(stem)) {
                        resultForPos.add(stem);
                        IIndexWord idxWord = dict.getIndexWord(stem, pos);
                        if (idxWord == null) continue;
                        List<IWordID> wordIDs = idxWord.getWordIDs();
                        if (wordIDs == null) continue;
                        IWordID wordID = wordIDs.get(0);
                        IWord iword = dict.getWord(wordID);
        
                        ISynset synonyms = iword.getSynset();
                        List<IWord> iRelatedWords = synonyms.getWords();
                        if (iRelatedWords != null) {
                            for (IWord iRelatedWord : iRelatedWords) {
                                String relatedWord = iRelatedWord.getLemma();
                                if (!resultForPos.contains(relatedWord))
                                    resultForPos.add(relatedWord);
                            }
                        }
                        
                        List<ISynsetID> hypernymIDs = synonyms.getRelatedSynsets();
                        if (hypernymIDs != null) {
                            for (ISynsetID relatedSynsetID : hypernymIDs) {
                                ISynset relatedSynset = dict.getSynset(relatedSynsetID);
                                if (relatedSynset != null) {
                                    iRelatedWords = relatedSynset.getWords();
                                    if (iRelatedWords != null) {
                                        for (IWord iRelatedWord : iRelatedWords) {
                                            String relatedWord = iRelatedWord.getLemma();
                                            if (!resultForPos.contains(relatedWord))
                                                resultForPos.add(relatedWord);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (String relatedWord : resultForPos) {
                    if (relatedWord.length() > 3
                            && !relatedWord.contains("-")
                            && !result.contains(relatedWord)) {
                        // TODO: Hack alert!
                        // The - check is to prevent lucene from interpreting hyphenated words as negative search terms
                        // Fix!
                        result.add(relatedWord);
                    }
                }
            }
        } finally {
            dict.close();
        }
        return result;
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
    Directory index(Analyzer analyzer, InputStream csvFile) throws IOException {
        if (!INDEX_FOLDER.exists()) { INDEX_FOLDER.mkdirs(); }
        File index = new File(INDEX_FOLDER, INDEX_FILE_PREFIX + System.currentTimeMillis());
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_CURRENT, analyzer);
        Directory directory = null;
        IndexWriter iwriter = null;
        try {
            directory = FSDirectory.open(index);
            iwriter = new IndexWriter(directory, config);
            CSVReader<String[]> reader = new CSVReaderBuilder<String[]>(new InputStreamReader(csvFile)).strategy(CSVStrategy.UK_DEFAULT).entryParser(new DefaultCSVEntryParser()).build();
            List<String> header = reader.readHeader();
            if (header != null) {
                String[] line;
                while ((line = reader.readNext()) != null) {
                    Document doc = new Document();
                    for (int i = 0; i < header.size(); i++) {
                        String fieldName = header.get(i);
                        String fieldValue = line[i];
                        doc.add(new Field(fieldName, fieldValue, TextField.TYPE_STORED));
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
    List<ScoreDocument> search(Analyzer analyzer, Directory index, String searchField, String searchTerm) throws IOException, ParseException {
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
                Document doc = isearcher.doc(hits[i].doc);
                double score = hits[i].score;
                results.add(new ScoreDocument(score, doc));
            }
        } finally {
            ireader.close();
        }
        Collections.sort(results);
        return results;
    }
    
    /**
     * Struct containing Score and document represented as Map<String, String>
     */
    @XmlRootElement
    public static final class ScoreDocument implements Comparable<ScoreDocument> {
        double score;
        Map<String, String> document;

        public ScoreDocument(double score, Document doc) {
            this.score = score;
            document = new LinkedHashMap<String, String>();
            for (IndexableField field : doc.getFields()) {
                document.put(field.name(), doc.get(field.name()));
            }
        }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public Map<String, String> getDocument() { return document; }
        public void setDocument(Map<String, String> document) { this.document = document; }
        
        @Override
        // sort by descending score
        public int compareTo(ScoreDocument o) { return Double.valueOf(o.score).compareTo(score); }
        @Override
        public String toString() { return score + (document.containsKey("Title") ? ": " + document.get("Title") : ""); }
    }
}
