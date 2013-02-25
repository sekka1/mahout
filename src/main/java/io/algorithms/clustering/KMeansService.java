	/**
 * 
 */
package io.algorithms.clustering;

import io.algorithms.util.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiError;
import com.wordnik.swagger.annotations.ApiErrors;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
/**
 * @author Anthony Yee
 *
 */
@Path("/kmeans")
@Api(value = "/kmeans", description = "Operations to run K-Means algorithms")
@Produces({MediaType.APPLICATION_JSON})
public class KMeansService {
	
	private static File PATH_APP;
	private static File PATH_EXTRACTED_ARCHIVE;
	private static File PATH_SEQUENCE_FILES;
	private static File PATH_VECTOR_FILES;
	private static File PATH_OUTPUT;
	private static File SEINFELD_ARCHIVE = null;
	static {
		try {
			PATH_APP = new File(Thread.currentThread().getContextClassLoader().getResource("/").toURI());
			PATH_EXTRACTED_ARCHIVE = new File(PATH_APP.getCanonicalPath() + File.separator + "kmeans/extracted-archive");
			PATH_SEQUENCE_FILES = new File(PATH_APP.getCanonicalPath() + File.separator + "kmeans/sequence-files");
			PATH_VECTOR_FILES = new File (PATH_APP.getCanonicalPath() + File.separator + "kmeans/vectors");
			PATH_OUTPUT = new File(PATH_APP.getCanonicalPath() + File.separator + "kmeans/output");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
			
	private static final Logger log = LoggerFactory.getLogger(KMeansService.class);
	private static final Map<String, String> exampleDatasetMap = new HashMap<String, String>();
	private static final String DATASET_SEINFELD = "seinfeld";
	static {
		exampleDatasetMap.put(DATASET_SEINFELD, "5555");
	}
	
	// This method is called if TEXT_PLAIN is request
	@GET
	@Path("/seinfeld")
	@Produces(MediaType.TEXT_PLAIN)
	public String introduceSeinfeldExample() {
		return "Execute example to demonstrate how to cluster Seinfeld episodes.  Thanks to Frank Scholten @ Trifork for this example" + 
				"http://blog.trifork.nl/2011/04/04/how-to-cluster-seinfeld-episodes-with-mahout/";
	}
	
	public class InternalServerException extends WebApplicationException {
	     public InternalServerException(String message) {
	         super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).type(MediaType.TEXT_PLAIN).build());
	     }
	}
	
	static private File uploadedArchive;
	
	@POST
	@Path("/archive")
	@ApiOperation(value="Archive of input data to K-Means algorithms")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(	
		@FormDataParam("file") 
		InputStream uploadedInputStream,
		@FormDataParam("file") 
		FormDataContentDisposition fileDetail)
	{
		try {
			File archivedir = new File(Thread.currentThread().getContextClassLoader().getResource("clustering").getPath() + File.separator + "archive");
			archivedir.mkdirs();
			uploadedArchive = new File(archivedir + File.separator + fileDetail.getFileName());
			OutputStream out = new FileOutputStream(uploadedArchive);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Uploaded archive " + fileDetail.getFileName() + " to " + uploadedArchive.getAbsolutePath());
		return Response.status(200).entity("Uploaded archive " + fileDetail.getFileName()).build();
	}
	
	@POST
	@ApiOperation(value = "Expand dataset into sequence files, convert into vectors and then run K-Means clustering algorithm")
	@ApiErrors(value= {@ApiError(code = 500, reason = "Internal Server Error"), @ApiError(code=400, reason="No archive uploaded")})
	public Response run(
	    @ApiParam(value="Authentication token", required=true)
	    @QueryParam("authToken")
	    @FormParam(value="authToken")
	    String authToken,
	    
	    @ApiParam(value="Calling algorithms.io API server", required=true)
	    @QueryParam("algoServer")
	    @FormParam(value="algoServer")
	    String algoServer,
	    
	    @ApiParam(value="Datasource ID", required=true)
	    @QueryParam("datasources")
	    @FormParam("datasources")
	    String datasources,
	    
		@ApiParam(value = "Max n-gram size used to generate sparse vectors") 
		@QueryParam("maxNGramSize")
	    @FormParam("maxNGramSize")
		String maxNGramSize,
		
		@ApiParam(value = "Min document frequency") 
	    @QueryParam("minDF") 
	    @FormParam("minDF")
	    String minDF,
		
	    @ApiParam(value = "The max percentage of docs for the DF.  Can be used to remove really high frequency terms."
				+ " Expressed as an integer between 0 and 100. Default is 99.  If maxDFSigma is also set, "
				+ "it will override this value.") 
	    @QueryParam("maxDF")
	    @FormParam("maxDF")
	    String maxDF,
		
		@ApiParam(value = "The kind of weight to use. Currently TF or TFIDF") 
		@QueryParam("weight")
	    @FormParam("weight")
		String weight,	
		
		@ApiParam(value = "The norm to use, expressed as either a float or \"INF\" if you want to use the Infinite norm.  "
				+ "Must be greater or equal to 0.  The default is not to normalize") 
		@QueryParam("norm")
	    @FormParam("norm")
		String norm,
		
		@ApiParam(value="Fully qualified class name of analyzer",
			allowableValues="org.apache.mahout.analysis.SeinfeldScriptAnalyzer," +
					"org.apache.mahout.text.MailArchivesClusteringAnalyzer," +
					"org.apache.mahout.text.wikipedia.WikipediaAnalyzer")
		@QueryParam("analyzerName")
	    @FormParam("analyzerName")
		String analyzerName,
		
		@ApiParam(value = "Maximum number of iterations which once reached, iterations will stop",
		defaultValue="10",
		allowableValues="range[1,20]") 
		@QueryParam("maxIter")
	    @FormParam("maxIter")
		String maxIter,
		
		@ApiParam(value = "The k in k-Means.  If specified, then a random selection of k Vectors will be chosen"
                + " as the centroid and written to the clusters input path.",
                defaultValue="100",
                allowableValues="range[1,100]",
                required=true) 
		@QueryParam("numClusters")
	    @FormParam("numClusters")
		String numClusters,

		@ApiParam(value = "Distance measure used to calculate distances from centroids",
			defaultValue="SquaredEuclidean", 
			allowableValues="org.apache.mahout.common.distance.SquaredEuclidean," + 
				"org.apache.mahout.common.distance.Euclidean," +
				"org.apache.mahout.common.distance.TanimotoDistanceMeasure," +
				"org.apache.mahout.common.distance.CosineDistanceMeasure," +
				"org.apache.mahout.common.distance.WeightedManhattanDistanceMeasure")
		@QueryParam("distanceMeasure")
	    @FormParam("distanceMeasure")
		String distanceMeasure,
		
		@ApiParam(value = "How many top feature words to show",
			defaultValue="5",
			required=true) 
		@QueryParam("numWords")
	    @FormParam("numWords")
		String numWords,
    
		@ApiParam(value = "Use for testing this endpoint with canned dataset",
			required = false,
			defaultValue="Seinfeld",
			allowableValues="Seinfeld,Reuters")
	    @QueryParam("test")
	    @FormParam("test")
	    String test
	)
	{
		System.out.println("algoServer=[" + algoServer + "]");
		System.out.println("authToken=[" + authToken + "]");
		String datasource = null;
		
		if (test != null) {
			if (exampleDatasetMap.get(test.toLowerCase()) == null) {
				return Response.status(400)
						.entity("Test specified [" + test + "] is not a valid test")
						.type(MediaType.APPLICATION_JSON)
						.build();
			}
			try {
				uploadedArchive = new File(Thread.currentThread()
						.getContextClassLoader()
						.getResource("clustering/seinfeld/seinfeld-scripts-preprocessed.tar.gz")
						.toURI());
			} catch (URISyntaxException e) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
		else {
			// test if datasource string is in format of an integer
			int dsid = -1;

			try {
				dsid = Integer.parseInt(datasources);
			}
			catch (NumberFormatException e) {
			}
			if (dsid <= 0) {
				JSON dsArray = JSONSerializer.toJSON(datasources);
				if (!(dsArray instanceof JSONArray)) {
					return Response.status(Status.BAD_REQUEST)
							.entity("Invalid datasources parameter [" + datasources + "]")
							.build();
				}
				datasource = ((JSONArray)dsArray).getString(0);
			}
			else {
				// parameter string is just an integer instead of a json array
				datasource = datasources;
			}
			System.out.println("DATASOURCE = [" + datasource + "]");
			
			try {

				uploadedArchive = IOUtils.downloadFileFromAPI(authToken,
						algoServer, datasource);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!uploadedArchive.exists()) {
				return Response.status(Status.NOT_FOUND)
						.entity("Dataset [" + datasource + "] download failed")
						.type(MediaType.APPLICATION_JSON)
						.build();
			}
		}	
		
		String dataPath = null;
		try {
			PATH_APP = new File(Thread.currentThread().getContextClassLoader().getResource("/").toURI());
			dataPath = PATH_APP.getCanonicalPath() + File.separator + "kmeans" + "-" + authToken + "-" + (test!=null ? test.toLowerCase() : datasource);
			PATH_EXTRACTED_ARCHIVE = new File(dataPath + File.separator + "extracted-archive");
			PATH_SEQUENCE_FILES = new File(dataPath + File.separator + "sequence-files");
			PATH_VECTOR_FILES = new File(dataPath + File.separator + "vectors");
			PATH_OUTPUT = new File(dataPath + File.separator + "output");

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// create a lock file if not exist
		File lockfile = new File(dataPath + "-lock");
		if (lockfile.exists()) {
			return Response.status(Status.UNAUTHORIZED)
					.entity("There's another kmeans process running with authToken [ " 
							+ authToken + "] and dataset [" + datasource + "]")
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
		try {
			lockfile.createNewFile();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		final TarGZipUnArchiver ua = new TarGZipUnArchiver();
		ua.enableLogging(new ConsoleLogger(
				org.codehaus.plexus.logging.Logger.LEVEL_INFO, "console"));
		if (PATH_EXTRACTED_ARCHIVE.exists()) {
			try {
				deleteRecursive(PATH_EXTRACTED_ARCHIVE);
			} catch (FileNotFoundException e1) {
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

		PATH_EXTRACTED_ARCHIVE.mkdirs();
		
		System.out.println("Extracting content of " + uploadedArchive);
		log.info("Extracting content of " + uploadedArchive.getName() + " to directory " + PATH_EXTRACTED_ARCHIVE);
		ua.setSourceFile(uploadedArchive);
		ua.setDestDirectory(PATH_EXTRACTED_ARCHIVE);
		ua.extract();
		
		// Check if the needed parameters are present before proceeding
		StringBuffer missParamError = new StringBuffer();
		if (analyzerName == null || maxNGramSize == null || maxDF == null || minDF == null || weight == null || norm == null) {
			missParamError.append("Missing one of these parameters (\"maxNGramSize\", \"maxDF\", \"minDF\", \"weight\", \"norm\")" +
					" needed for generating sparse vectors\n");		
		}
		if (numWords == null || numClusters == null || distanceMeasure == null) {
			missParamError.append("Missing one of these clustering parameters (numClusters, distanceMeasure, numWords)");
		}
		if (missParamError.length() > 0) {
			return Response.status(400).entity(missParamError.toString()).build();
		}

 	
    	try {
	    	
	    	ToolRunner.run(new SequenceFilesFromDirectory(), new String[] {
	    		"--input", PATH_EXTRACTED_ARCHIVE.getCanonicalPath(),
	    		"--output", PATH_SEQUENCE_FILES.getCanonicalPath() ,
	    		"--charset","utf-8",
	    		"--overwrite"
	    	});
	    	
	    	ToolRunner.run(new SparseVectorsFromSequenceFiles(), new String [] {
					"--input", PATH_SEQUENCE_FILES.getCanonicalPath(),
					"--output", PATH_VECTOR_FILES.getCanonicalPath(),
	                "--maxNGramSize", maxNGramSize,
	                "--namedVector",
	                "--minDF", minDF,
	                "--maxDFPercent", maxDF,
	                "--weight", weight,
	                "--norm", norm,
	                "--analyzerName", analyzerName,
	                "--overwrite"
				});
	    	
 			ToolRunner.run(new KMeansDriver(), new String[] {
				"--input", PATH_VECTOR_FILES.getCanonicalPath() + File.separator + "tfidf-vectors",
				"--output", PATH_OUTPUT.getCanonicalPath() + File.separator + "clusters",
				"--clusters", PATH_OUTPUT.getCanonicalPath() + File.separator + "initialclusters",
				"--maxIter", maxIter,
				"--numClusters", numClusters,
				"--distanceMeasure", distanceMeasure,
				"--clustering",
				"--overwrite",
			});
			

			ToolRunner.run(new ClusterDumper(), new String[] {
				"--seqFileDir", PATH_OUTPUT.getCanonicalPath() + File.separator + "clusters/clusters-1",
				"--output", PATH_OUTPUT.getCanonicalPath() + File.separator + "dump",
				"--pointsDir", PATH_OUTPUT.getCanonicalPath() + File.separator + "clusters/clusteredPoints",
				"--numWords", numWords,
				"--dictionary", PATH_VECTOR_FILES.getCanonicalPath() + File.separator + "dictionary.file-0",
				"--dictionaryType", "sequencefile"
			});
		} catch (ClassNotFoundException e) {
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
		             .entity(e.getMessage() + " not found").type(MediaType.TEXT_PLAIN).build());
//			return Response.status(400).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(500).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Response.status(500).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		}
    	
		File output;
		String bigstring = null;
		try {
			output = new File(PATH_OUTPUT.getCanonicalPath() + File.separator + "dump");
			bigstring = FileUtils.readFileToString(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		lockfile.delete();
		
        return Response.status(200).entity(bigstring).type(MediaType.APPLICATION_JSON).build(); 
	}
	
	@GET
	@Path("/clusterdumper")
	@ApiOperation(value="Dump cluster as a JSON")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dumpCluster(
		@ApiParam(value = "How many top feature words to show",
			defaultValue="5") 
		@FormParam("numWords")
		@QueryParam("numWords") 
		String numWords)
	{
		
		try {
			ToolRunner.run(new ClusterDumper(), new String[] {
				"--seqFileDir", PATH_OUTPUT.getCanonicalPath() + File.separator + "clusters/clusters-1",
				"--output", PATH_OUTPUT.getCanonicalPath() + File.separator + "dump",
				"--pointsDir", PATH_OUTPUT.getCanonicalPath() + File.separator + "clusters/clusteredPoints",
				"--numWords", numWords,
				"--dictionary", PATH_VECTOR_FILES.getCanonicalPath() + File.separator + "dictionary.file-0",
				"--dictionaryType", "sequencefile"
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File output;
		String bigstring = null;
		try {
			output = new File(PATH_OUTPUT.getCanonicalPath() + File.separator + "dump");
			bigstring = FileUtils.readFileToString(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
        return Response.status(200).entity(bigstring).type(MediaType.APPLICATION_JSON).build(); 
	}
	
	
    public static boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }
     
    
    /**
     * @param args
     */
    public static void main(String[] args) {
		System.out.println(Thread.currentThread().getContextClassLoader().getResource("clustering/seinfeld").getPath());

//    	try {
//			cleanSeinfeldData();
//			sequenceFilesFromArchive(SEINFELD_ARCHIVE, PATH_IN_SEINFELD_SCRIPTS, 
//					PATH_OUT_SEINFELD_SEQFILES, false);
//	    	ToolRunner.run(new SparseVectorsFromSequenceFiles(), new String [] {
//					"--input", PATH_OUT_SEINFELD_SEQFILES.getCanonicalPath(),
//					"--output", PATH_OUT_SEINFELD_VECTORS.getCanonicalPath(),
//	                "--maxNGramSize", "2",
//	                "--namedVector",
//	                "--minDF", "4",
//	                "--maxDFPercent", "75",
//	                "--weight", "TFIDF",
//	                "--norm", "2",
//	                "--analyzerName", "org.apache.mahout.analysis.SeinfeldScriptAnalyzer"
//				});
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
		
	}
    
	@POST
	@Path("/simple")
	public Response simplePost(
			@ApiParam(value="Simple parameter for testing simple POST") String simple)
	{
		return Response.status(200).entity("You have successfully sent a simple POST request with parameter [" + simple + "]").type(MediaType.TEXT_PLAIN).build();	
	}
	
}
