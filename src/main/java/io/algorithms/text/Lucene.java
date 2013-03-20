/*
* Copyright 2013 Algorithms.io. All Rights Reserved.
*/
package io.algorithms.text;

import io.algorithms.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.lf5.util.StreamUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
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
 * Exposes index and search via lucene.
 */
public class Lucene {
    static final Version LUCENE_CURRENT = Version.LUCENE_36;
    static final File INDEX_FOLDER = new File(IOUtils.getTmpFolder(), "lucene-index");
    static final String INDEX_FILE_PREFIX = LUCENE_CURRENT + "-";
    static final String FIELD_NAME = "Name", FIELD_TEXT = "Text";

    Analyzer analyzer = new StandardAnalyzer(LUCENE_CURRENT);
    
    /**
     * This method indexes a set of text documents in a ZIP file.
     * @param zipFile archive containing text documents
     * @return the index
     * @throws IOException
     */
    Directory indexZip(File zipFile, File indexDir) throws IOException {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = null;
            List<Document> documents = new ArrayList<Document>();
            while ((ze = zis.getNextEntry()) != null) {
                Document doc = new Document();
                doc.add(new Field(FIELD_NAME, ze.getName(), Field.Store.YES, Field.Index.ANALYZED));
                String text = new String(StreamUtils.getBytes(zis), Charset.defaultCharset());
                doc.add(new Field(FIELD_TEXT, text, Store.YES, Field.Index.ANALYZED));
                documents.add(doc);
            }
            return index(documents, indexDir);
        } finally {
            if (zis != null) {
                zis.close();
            }
        }
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
    Directory indexCSV(File csvFile, File indexDir) throws IOException {
        CSVReader<String[]> reader = new CSVReaderBuilder<String[]>(new InputStreamReader(new FileInputStream(csvFile))).strategy(CSVStrategy.UK_DEFAULT).entryParser(new DefaultCSVEntryParser()).build();
        List<String> header = reader.readHeader();
        if (header != null) {
            List<Document> documents = new ArrayList<Document>();
            String[] line;
            while ((line = reader.readNext()) != null) {
                Document document = new Document();
                for (int i = 0; i < header.size(); i++) {
                    String fieldName = header.get(i);
                    String fieldValue = line[i];
                    document.add(new Field(fieldName, fieldValue, Field.Store.YES, Field.Index.ANALYZED));
                }
                documents.add(document);
            }
            return index(documents, indexDir);
        }
        return null;
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
    Directory index(List<Document> documents, File indexDir) throws IOException {
        if (!indexDir.exists()) { indexDir.mkdirs(); }
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_CURRENT, analyzer);
        Directory directory = null;
        IndexWriter iwriter = null;
        try {
            directory = FSDirectory.open(indexDir);
            iwriter = new IndexWriter(directory, config);
            for (Document document : documents) {
                iwriter.addDocument(document);
            }
        } finally {
            if (iwriter != null) iwriter.close();
        }
        return directory;
    }

    private File getIndexDirForFile(File inputFile) throws IOException, NoSuchAlgorithmException {
        String sha = IOUtils.getSha1HashAsHexString(inputFile);
        return new File(INDEX_FOLDER, INDEX_FILE_PREFIX + sha);
    }
    
    public List<ScoreDocument> searchCSV(File csvFile, String searchField, String searchTerm) throws IOException, ParseException, NoSuchAlgorithmException {
        File dir = getIndexDirForFile(csvFile);
        Directory d = dir.exists() ? FSDirectory.open(dir) : indexCSV(csvFile, dir);
        return search(d, searchField, searchTerm);
    }
    
    public List<ScoreDocument> searchZip(File zipFile, String searchTerm) throws IOException, ParseException, NoSuchAlgorithmException {
        File dir = getIndexDirForFile(zipFile);
        Directory d = dir.exists() ? FSDirectory.open(dir) : indexZip(zipFile, dir);
        return search(d, FIELD_TEXT, searchTerm);
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
    List<ScoreDocument> search(Directory index, String searchField, String searchTerm) throws IOException, ParseException {
        IndexReader ireader = null;
        IndexSearcher isearcher = null;
        List<ScoreDocument> results = new ArrayList<ScoreDocument>();
        try {
            // Now search the index:
            ireader = IndexReader.open(index);
            isearcher = new IndexSearcher(ireader);
            if (searchField == null) searchField = FIELD_TEXT;
            
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
            isearcher.close();
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
            for (Fieldable field : doc.getFields()) {
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
