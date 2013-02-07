package io.algorithms.clustering;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import io.algorithms.clustering.SeinfeldScriptAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.apache.commons.io.FileUtils.*;

/**
 * Indexes seinfeld scripts.
 *
 * @author Frank Scholten
 */
public class SeinfeldScriptsIndexer {
  private static final String ID_FIELD = "id";
  private static final String CONTENT_FIELD = "content";

  private String indexPath;
  private List<File> scripts;
  private Analyzer analyzer;

  public SeinfeldScriptsIndexer(String indexPath, Analyzer analyzer, List<File> scripts) {
    this.indexPath = indexPath;
    this.analyzer = analyzer;
    this.scripts = scripts;
  }

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    File scriptsDirectory = new File("src/main/resources/seinfeld-scripts");
    List<File> scripts = (List<File>) FileUtils.listFiles(scriptsDirectory, new String[]{"txt"}, false);

    SeinfeldScriptsIndexer seinfeldScriptsIndexer = new SeinfeldScriptsIndexer("target/seinfeld-scripts-index", new SeinfeldScriptAnalyzer(), scripts);
    seinfeldScriptsIndexer.buildIndex();
  }

  public Directory buildIndex() throws Exception {
    try {
      Directory index = FSDirectory.open(new File(indexPath));
      IndexWriter indexWriter = new IndexWriter(index, analyzer, IndexWriter.MaxFieldLength.LIMITED);

      System.out.println("Indexing " + scripts.size() + " scripts...");

      for (File script : scripts) {
        Document scriptDocument = new Document();
        Field idField = new Field(ID_FIELD, script.getName(), Field.Store.YES, Field.Index.NO, Field.TermVector.NO);
        Field contentField = new Field(CONTENT_FIELD, readFileToString(script), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES);

        scriptDocument.add(idField);
        scriptDocument.add(contentField);

        indexWriter.addDocument(scriptDocument);
      }
      indexWriter.commit();
      indexWriter.optimize();
      indexWriter.close();

      System.out.println("Done!");

      return index;
    } catch (IOException e) {
      throw new java.lang.RuntimeException("Could not read script file", e);
    }
  }
}