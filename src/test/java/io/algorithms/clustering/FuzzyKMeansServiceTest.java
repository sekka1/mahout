package io.algorithms.clustering;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.test.framework.JerseyTest;
import io.algorithms.clustering.SeinfeldScriptAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class FuzzyKMeansServiceTest extends JerseyTest
{
    private ClientResponse response;

    public FuzzyKMeansServiceTest() throws Exception
    {
        super("io.algorithms.clustering");
    }

	public final void GtestSeinfeldPostWithForm()
	{
        WebResource wr = client().resource("http://localhost:9998").path("fuzzykmeans");
        Analyzer a = new SeinfeldScriptAnalyzer();
        Form form = new Form();
        form.add("algoServer", "jersey-test-server");
        form.add("authToken", "jersey-test-token");
        form.add("test", "seinfeld");
        form.add("datasources", "doesnt matter");
// TODO: Calling SeinfeldScriptAnalyzer returns
//        java.lang.IllegalStateException: java.lang.reflect.InvocationTargetException
//        at org.apache.mahout.common.ClassUtils.instantiateAs(ClassUtils.java:70)
//        at org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles.run(SparseVectorsFromSequenceFiles.java:204)
//        at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:65)
//        at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:79)
//        at io.algorithms.clustering.FuzzyKMeansService.run(FuzzyKMeansService.java:456)
//        java.lang.AssertionError: TokenStream implementation classes or at least their incrementToken() implementation must be final
//        at org.apache.lucene.analysis.TokenStream.assertFinal(TokenStream.java:119)
//        at org.apache.lucene.analysis.TokenStream.&lt;init&gt;(TokenStream.java:100)
//        java.lang.IllegalStateException: Job failed!
//            at org.apache.mahout.vectorizer.DocumentProcessor.tokenizeDocuments(DocumentProcessor.java:95)
//        at org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles.run(SparseVectorsFromSequenceFiles.java:253)
//        at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:65)
//        at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:79)
//        at io.algorithms.clustering.FuzzyKMeansService.run(FuzzyKMeansService.java:456)

        form.add("analyzerName", "io.algorithms.clustering.SeinfeldScriptAnalyzer");
        form.add("maxNGramSize", "2");
        form.add("maxDF", "75");
        form.add("minDF", "4");
        form.add("weight", "TFIDF");
        form.add("norm", "2");
        form.add("numWords", "10");
        form.add("numClusters", "10");
        form.add("maxIter", "10");
        form.add("distanceMeasure", "org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure");
        form.add("convergenceDelta", 0.5);
        form.add("m", 2);
        form.add("method", "mapreduce");
        form.add("emitMostLikely", true);
        form.add("threshold", "0");
        String resp = wr.post(String.class, form);
        System.out.println("KMeansServiceTest.testSeinfeldPost");
        System.out.println("KMeansServiceTest.testSeinfeldPost  " + resp);
//        assertEquals("Hello World", responseMsg);
 
	}

}
