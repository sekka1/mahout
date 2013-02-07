	/**
 * 
 */
package io.algorithms.clustering;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import io.algorithms.clustering.ClusterDumper;
import io.algorithms.util.IOUtils;

import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.NotFoundException;
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
	
	private static final String PATH_DATA_PREFIX = "data";
	private static final File PATH_UPLOADED_ARCHIVE = new File(PATH_DATA_PREFIX + File.separator + "uploaded-archive");
	private static final File PATH_EXTRACTED_ARCHIVE = new File(PATH_DATA_PREFIX + File.separator + "extracted-archive");
	private static final File PATH_SEQUENCE_FILES = new File(PATH_DATA_PREFIX + File.separator + "sequence-files");
	private static final File PATH_VECTOR_FILES = new File (PATH_DATA_PREFIX + File.separator + "vectors");
	private static final File PATH_OUTPUT = new File(PATH_DATA_PREFIX + File.separator + "output");
	private static final File PATH_IN_SEINFELD_SCRIPTS = new File("seinfeld-scripts-preprocessed");
	private static File SEINFELD_ARCHIVE = null;
	static {
		PATH_UPLOADED_ARCHIVE.mkdirs();
		PATH_EXTRACTED_ARCHIVE.mkdirs();
		PATH_SEQUENCE_FILES.mkdir();
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
	
	@POST
	@Path("/seinfeld")
	@ApiOperation(value = "Execute clustering on Seinfeld episodes")
	@ApiErrors(value= {@ApiError(code = 500, reason = "Internal Server Error")})
	public Response runSeinfeldExample(
			@ApiParam(value = "Max n-gram size used to generate sparse vectors") 
			@QueryParam("maxNGramSize") 
			String maxNGramSize,
			@ApiParam(value = "Min document frequency") @QueryParam("minDF") String minDF,
			@ApiParam(value = "The max percentage of docs for the DF.  Can be used to remove really high frequency terms."
					+ " Expressed as an integer between 0 and 100. Default is 99.  If maxDFSigma is also set, "
					+ "it will override this value.") @QueryParam("maxDF") String maxDF,
			
			@ApiParam(value = "The kind of weight to use. Currently TF or TFIDF") 
			@QueryParam("weight")
			String weight,
			
			@ApiParam(value = "The norm to use, expressed as either a float or \"INF\" if you want to use the Infinite norm.  "
					+ "Must be greater or equal to 0.  The default is not to normalize") 
			@QueryParam("norm") 
			String norm,
			
			@ApiParam(value = "The k in k-Means.  If specified, then a random selection of k Vectors will be chosen"
			                + " as the centroid and written to the clusters input path.",
			                defaultValue="100",
			                allowableValues="range[1,100]",
			                required=true) 
			@QueryParam("numClusters") 
			String numClusters,
			
			@ApiParam(value = "Maximum number of iterations which once reached, iterations will stop",
					defaultValue="10",
					allowableValues="range[1,20]",
					required=true) 
			@QueryParam("maxIter")  
			String maxIter,
			
			@ApiParam(value = "Distance measure used to calculate distances from centroids",
					defaultValue="SquaredEuclidean", 
					allowableValues="SquaredEuclidean,Euclidean,Mahalanobis") 
			@QueryParam("distanceMeasure") 
			String distanceMeasure,
			
			@ApiParam(value = "How many top feature words to show",
				defaultValue="5") 
			@QueryParam("numWords") 
			String numWords,
			
			@ApiParam(value="Regenerate vectors", required=false, defaultValue="false")
			@QueryParam("generateVectors")
			String generateVectors
			
			)
	throws NotFoundException, FileNotFoundException {	

		// Clean up seinfeld example output data
//		File dir = new File(".");
//		FileFilter fileFilter = new FileFilter() {
//			public boolean accept(File arg0) {
//				return (arg0.isDirectory() && arg0.getName().startsWith(SEINFELD_PREFIX));
//			}
//		};
//		File[] files = dir.listFiles(fileFilter);
//		for (int i = 0; i < files.length; i++) {
//			deleteRecursive(files[i]);
//		}
//		
		try {
			SEINFELD_ARCHIVE = new File(Thread.currentThread().getContextClassLoader().getResource("seinfeld-scripts-preprocessed.tar.gz").toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String path = Thread.currentThread().getContextClassLoader().getResource("clustering/seinfeld").getPath();
		
		try {
			
			// only do this if need to regenerate the sequence files
//	    	final TarGZipUnArchiver ua = new TarGZipUnArchiver();
//	    	ua.enableLogging(new ConsoleLogger( org.codehaus.plexus.logging.Logger.LEVEL_INFO, "console" ));
//	    	System.out.println("Extracting content of " + SEINFELD_ARCHIVE.getCanonicalPath() + " to directory " + path);
//	    	ua.setSourceFile(SEINFELD_ARCHIVE);
//	    	File destdir = new File(path);
//	    	destdir.mkdirs();
//	    	ua.setDestDirectory(destdir);
//	    	ua.extract();
//	    	
//	    	ToolRunner.run(new SequenceFilesFromDirectory(), new String[] {
//	    		"--input", path + File.separator + "seinfeld-scripts-preprocessed",
//	    		"--output", path + File.separator + "out-seinfeld-seqfiles",
//	    		"--charset","utf-8",
//	    		"--overwrite"
//	    	});

			if (Boolean.parseBoolean(generateVectors)) {
				// only generate vectors if prompted
				if (maxNGramSize == null || minDF == null || maxDF == null || weight == null || norm == null) {
			        return Response.status(400).entity("Parameter \"generateVectors\" has been set, parameters maxNGramSize, minDF, " + 
			        		"maxDFPercent, weight and norm need to be set").type("application/text").build();
				}
				ToolRunner.run(new SparseVectorsFromSequenceFiles(), new String [] {
					"--input", path + File.separator + "out-seinfeld-seqfiles",
					"--output", path + File.separator + "out-seinfeld-vectors",
	                "--maxNGramSize", maxNGramSize,
	                "--namedVector",
	                "--minDF", minDF,
	                "--maxDFPercent", maxDF,
	                "--weight", weight,
	                "--norm", norm,
	                "--analyzerName", "io.algorithms.clustering.SeinfeldScriptAnalyzer",
	                "--overwrite"
				});
			}
			
			ToolRunner.run(new KMeansDriver(), new String[] {
				"--input", path + File.separator + "out-seinfeld-vectors/tfidf-vectors",
				"--output", path + File.separator + "out-seinfeld-kmeans/clusters",
				"--clusters", path + File.separator + "out-seinfeld-kmeans/initialclusters",
				"--maxIter", maxIter,
				"--numClusters", numClusters,
				"--distanceMeasure", "org.apache.mahout.common.distance."+distanceMeasure+"DistanceMeasure",
				"--clustering",
				"--overwrite",
			});
			
			ToolRunner.run(new ClusterDumper(), new String[] {
				"--seqFileDir", path + File.separator + "out-seinfeld-kmeans/clusters/clusters-1",
				"--output", path + File.separator + "out-seinfeld-kmeans/clusters/dump",
				"--pointsDir", path + File.separator + "out-seinfeld-kmeans/clusters/clusteredPoints",
				"--numWords", numWords,
				"--dictionary", path + File.separator + "out-seinfeld-vectors/dictionary.file-0",
				"--dictionaryType", "sequencefile"
			});
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServerException(e.getMessage());
		}
		
		File output = new File(path + File.separator + "out-seinfeld-kmeans/clusters/dump");
		String bigstring = null;
		try {
			bigstring = FileUtils.readFileToString(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
        return Response.status(200).entity(bigstring).type("application/text").build(); 
	    // your resource logic 
	}
	
	public class ServerException extends WebApplicationException {
	     public ServerException(String message) {
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
	    String authToken,
	    
	    @ApiParam(value="Calling algorithms.io API server", required=true)
	    @QueryParam("algoServer")
	    String algoServer,
	    
	    @ApiParam(value="Example to run",
	    	allowableValues="Seinfeld Episodes")
	    @QueryParam("example")
	    String example,
	    
	    @ApiParam(value="Datasource ID", required=true)
	    @QueryParam("datasource")
	    String datasource,
	    
		@ApiParam(value = "Max n-gram size used to generate sparse vectors") 
		@QueryParam("maxNGramSize") 
		String maxNGramSize,
		
		@ApiParam(value = "Min document frequency") 
	    @QueryParam("minDF") 
	    String minDF,
		
	    @ApiParam(value = "The max percentage of docs for the DF.  Can be used to remove really high frequency terms."
				+ " Expressed as an integer between 0 and 100. Default is 99.  If maxDFSigma is also set, "
				+ "it will override this value.") 
	    @QueryParam("maxDF")
	    String maxDF,
		
		@ApiParam(value = "The kind of weight to use. Currently TF or TFIDF") 
		@QueryParam("weight")
		String weight,	
		
		@ApiParam(value = "The norm to use, expressed as either a float or \"INF\" if you want to use the Infinite norm.  "
				+ "Must be greater or equal to 0.  The default is not to normalize") 
		@QueryParam("norm") 
		String norm,
		
		@ApiParam(value="Fully qualified class name of analyzer",
			allowableValues="org.apache.mahout.analysis.SeinfeldScriptAnalyzer," +
					"org.apache.mahout.text.MailArchivesClusteringAnalyzer," +
					"org.apache.mahout.text.wikipedia.WikipediaAnalyzer")
		@QueryParam("analyzerName")
		String analyzerName,
		
		@ApiParam(value = "Maximum number of iterations which once reached, iterations will stop",
		defaultValue="10",
		allowableValues="range[1,20]") 
		@QueryParam("maxIter")  
		String maxIter,
		
		@ApiParam(value = "The k in k-Means.  If specified, then a random selection of k Vectors will be chosen"
                + " as the centroid and written to the clusters input path.",
                defaultValue="100",
                allowableValues="range[1,100]",
                required=true) 
		@QueryParam("numClusters") 
		String numClusters,

		@ApiParam(value = "Distance measure used to calculate distances from centroids",
			defaultValue="SquaredEuclidean", 
			allowableValues="org.apache.mahout.common.distance.SquaredEuclidean," + 
				"org.apache.mahout.common.distance.Euclidean," +
				"org.apache.mahout.common.distance.TanimotoDistanceMeasure," +
				"org.apache.mahout.common.distance.CosineDistanceMeasure," +
				"org.apache.mahout.common.distance.WeightedManhattanDistanceMeasure")
		@QueryParam("distanceMeasure") 
		String distanceMeasure,
		
		@ApiParam(value = "How many top feature words to show",
			defaultValue="5",
			required=true) 
		@QueryParam("numWords") 
		String numWords,
		
		@ApiParam(value = "Use for testing directly with local dataset rather than downloading", defaultValue="false")
	    @QueryParam("test")
	    String test
	)
	{
		if (test != null && Boolean.parseBoolean(test) == Boolean.TRUE) {
			try {
				uploadedArchive = new File(Thread.currentThread()
						.getContextClassLoader()
						.getResource("seinfeld-scripts-preprocessed.tar.gz")
						.toURI());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			String dsID = null;
			if (example!=null) {
				exampleDatasetMap.get(example.toLowerCase());
			}
			if (dsID == null) {
				dsID = datasource;
			}
			try {
				uploadedArchive = IOUtils.downloadFileFromAPI(authToken,
						algoServer, dsID);
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
				return Response.status(400)
						.entity("Dataset [" + dsID + "] download failed")
						.build();
			}
		}	
		
		final TarGZipUnArchiver ua = new TarGZipUnArchiver();
		ua.enableLogging(new ConsoleLogger(
				org.codehaus.plexus.logging.Logger.LEVEL_INFO, "console"));

		
		log.info(PATH_EXTRACTED_ARCHIVE + " created, cleaning up");
		try {
			deleteRecursive(PATH_EXTRACTED_ARCHIVE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	    
        return Response.status(200).entity(bigstring).type("application/text").build(); 
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

	// This method is called if TEXT_PLAIN is request
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayPlainTextHello() {
		return "Hello Cluster";
	}
	
}
