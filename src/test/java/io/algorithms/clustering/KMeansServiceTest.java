package io.algorithms.clustering;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;
import org.apache.commons.collections.map.MultiValueMap;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;

import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;

public class KMeansServiceTest extends JerseyTest 
{
    private ClientResponse response;

    public KMeansServiceTest() throws Exception 
    {
        super("io.algorithms.clustering");
    }
//
//	@Before
//	public void setUp() throws Exception {
//
//	}

//	@Test
//	public final void testRun() {
//		Form formdata = new Form();
//		formdata.add("algoServer", "foo");
//		formdata.add("authToken", "bar");
//		
//        WebResource webResource = client().resource("http://localhost:8080/");
//        JSONObject responseMsg = webResource.path("rest/kmeans")
//        		.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
//        		.accept(MediaType.APPLICATION_JSON_TYPE)
//        		.post(JSONObject.class, formdata);
//	}
	
//	@Test
//	public final void testSimplePost()
//	{
//        WebResource webResource = client().resource("http://localhost:8080/rest");
//        JSONObject responseMsg = webResource.path("simple")
//        		.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
//        		.accept(MediaType.APPLICATION_JSON_TYPE)
//        		.post(JSONObject.class);
//	}
	
	@Test
	public final void _testSeinfeldPostWithForm()
	{
        WebResource wr = client().resource("http://localhost:9998").path("kmeans");
        Form form = new Form();
        form.add("algoServer", "jersey-test-server");
        form.add("authToken", "jersey-test-token");
        form.add("test", "seinfeld");
        form.add("datasources", "doesnt matter");
        form.add("analyzerName", "org.apache.mahout.text.MailArchivesClusteringAnalyzer");
        form.add("maxNGramSize", "2");
        form.add("maxDF", "75");
        form.add("minDF", "4");
        form.add("weight", "TFIDF");
        form.add("norm", "2");
        form.add("numWords", "10");
        form.add("numClusters", "10");
        form.add("maxIter", "10");
        form.add("distanceMeasure", "org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure");
        String resp;
        resp = wr.post(String.class, form);
        System.out.println("KMeansServiceTest.testSeinfeldPost");
        System.out.println("KMeansServiceTest.testSeinfeldPost  " + resp);
//        assertEquals("Hello World", responseMsg);
 
	}

    @Test
    public final void testSayHello()
    {
        WebResource wr = client().resource("http://localhost:9998");
        Form form = new Form();
        form.add("what", "world");
        String resp = wr.path("hello").type(MediaType.APPLICATION_FORM_URLENCODED)
            .post(String.class, form);

    }

}
