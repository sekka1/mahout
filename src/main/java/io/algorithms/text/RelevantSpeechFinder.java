/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
*/
package io.algorithms.text;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

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

/**
 * Finds speeches given search terms.
 */
public class RelevantSpeechFinder {
    
    static final Version LUCENE_CURRENT = Version.LUCENE_40;

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
    public Map<String, Map<String, Map<Double, Map<String, String>>>> findRelevantSpeeches(File csvFile, String queryText) throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer(LUCENE_CURRENT);
        Directory index = index(analyzer, csvFile);
        Map<String, Map<String, Map<Double, Map<String, String>>>> results = new LinkedHashMap<String, Map<String, Map<Double, Map<String, String>>>>();
        StringTokenizer st = new StringTokenizer(queryText);
        while (st.hasMoreTokens()) {
            String word = st.nextToken();
            List<String> relatedWords = getRelatedWords(word);
            Map<String, Map<Double, Map<String, String>>> partialResults = new LinkedHashMap<String, Map<Double, Map<String, String>>>();
            for (String relatedWord : relatedWords) {
                Map<Double, Map<String, String>> searchResults = search(analyzer, index, "Transcript", relatedWord);
                partialResults.put(relatedWord, searchResults);
            }
            results.put(word, partialResults);
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
        result.add(word);
        IDictionary dict = new Dictionary(new File("/usr/share/wordnet"));
        try {
            dict.open();
            for (POS pos : EnumSet.of(POS.ADJECTIVE, POS.ADVERB, POS.NOUN, POS.VERB)) {
                IIndexWord idxWord = dict.getIndexWord(word, pos);
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
                        if (!result.contains(relatedWord))
                            result.add(relatedWord);
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
                                    if (!result.contains(relatedWord))
                                        result.add(relatedWord);
                                }
                            }
                        }
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
    Directory index(Analyzer analyzer, File csvFile) throws IOException {
        File index = new File("lucene-index-" + System.currentTimeMillis());
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_CURRENT, analyzer);
        Directory directory = null;
        IndexWriter iwriter = null;
        try {
            directory = FSDirectory.open(index);
            iwriter = new IndexWriter(directory, config);
            CSVReader<String[]> reader = new CSVReaderBuilder<String[]>(new FileReader(csvFile)).strategy(CSVStrategy.UK_DEFAULT).entryParser(new DefaultCSVEntryParser()).build();
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
    SortedMap<Double, Map<String, String>> search(Analyzer analyzer, Directory index, String searchField, String searchTerm) throws IOException, ParseException {
        IndexReader ireader = null;
        SortedMap<Double, Map<String, String>> results = new TreeMap<Double, Map<String, String>>(new Comparator<Double>() {
            public int compare(Double o1, Double o2) { return (o1 > o2) ? -1 : ((o2 > o1) ? 1 : 0); } } );
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
                Map<String, String> docMap = new LinkedHashMap<String, String>();
                for (IndexableField field : doc.getFields()) {
                    docMap.put(field.name(), doc.get(field.name()));
                }
                double score = hits[i].score;
                while (results.containsKey(score)) { score -= 1E-6; }
                results.put(Double.valueOf(hits[i].score), docMap);
            }
        } finally {
            ireader.close();
        }
        return results;
    }
}
