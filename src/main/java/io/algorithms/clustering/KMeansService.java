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
import java.util.zip.GZIPInputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.utils.clustering.ClusterDumper;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
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
	
	private static final String UPLOADED_ARCHIVE_LOCATION = "uploaded-archive";
	private static final String SEINFELD_PREFIX = "out-seinfeld";
	private static final File PATH_OUT_SEINFELD_VECTORS = new File(SEINFELD_PREFIX+ "-vectors");
	private static final File PATH_OUT_SEINFELD_SEQFILES = new File(SEINFELD_PREFIX + "-seqfiles");
	private static final File PATH_IN_SEINFELD_SCRIPTS = new File("seinfeld-scripts-preprocessed");
	private static final File SEINFELD_ARCHIVE = new File("seinfeld-scripts-preprocessed.tar.gz");
	private static final Logger log = LoggerFactory.getLogger(KMeansService.class);

	@POST
	@Path("/seinfeld-example")
	@ApiOperation(value = "Execute example to demonstrate how to cluster Seinfeld episodes.  Thanks to Frank Scholten @ Trifork for this example" + 
			"http://blog.trifork.nl/2011/04/04/how-to-cluster-seinfeld-episodes-with-mahout/")
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
			String distanceMeasure
			)
	throws NotFoundException, FileNotFoundException {	
		
		cleanSeinfeldData();

		try {
			sequenceFilesFromArchive(SEINFELD_ARCHIVE, 
					PATH_IN_SEINFELD_SCRIPTS,
					PATH_OUT_SEINFELD_SEQFILES, 
					false);
			sparseVectorsFromSequenceFiles(PATH_OUT_SEINFELD_SEQFILES, PATH_OUT_SEINFELD_VECTORS, 
					"2", "4", "75", "TFIDF", "2", "org.apache.mahout.analysis.SeinfeldScriptAnalyzer");
			Thread.sleep(5000);
			ToolRunner.run(new KMeansDriver(), new String[] {
				"--input", "out-seinfeld-vectors/tfidf-vectors",
				"--output", "out-seinfeld-kmeans/clusters",
				"--clusters", "out-seinfeld-kmeans/initialclusters",
				"--maxIter", "10",
				"--numClusters", "100",
				"--distanceMeasure", "org.apache.mahout.common.distance."+distanceMeasure+"DistanceMeasure",
				"--clustering",
				"--overwrite",
			});
			ToolRunner.run(new ClusterDumper(), new String[] {
				"--input", "out-seinfeld-kmeans/clusters/clusters-1",
				"--output", "out-seinfeld-kmeans/clusters/dump",
				"--pointsDir", "out-seinfeld-kmeans/clusters/clusteredPoints",
				"--numWords", "5",
				"--dictionary", "out-seinfeld-vectors/dictionary.file-0",
				"--dictionaryType", "sequencefile"
			});
		} catch (Exception e) {
			 throw new ServerException(e.getMessage());
		}
	    
        return Response.status(200).entity("You have summitted a request").type("application/json").build(); 
	    // your resource logic 
	}
	
	public class ServerException extends WebApplicationException {
	     public ServerException(String message) {
	         super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).type(MediaType.TEXT_PLAIN).build());
	     }
	}
	
	@POST
	@Path("/archive2seq")
	@ApiOperation(value = "Converts archive of raw input text files into a sequence file diectory")
	@ApiErrors(value= {@ApiError(code = 400, reason = "Invalid ID supplied"), @ApiError(code = 404, reason = "Pet not found") })
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response sequenceFilesFromArchive(
		@ApiParam(value = "Archive") 	
		@FormDataParam("archive") 
		InputStream uploadedInputStream,
		@FormDataParam("archive") 
		FormDataContentDisposition fileDetail)
	{
		String uploadedArchiveLocation = UPLOADED_ARCHIVE_LOCATION + fileDetail.getFileName();	 
		try {
			OutputStream out = new FileOutputStream(new File(uploadedArchiveLocation));
			int read = 0;
			byte[] bytes = new byte[1024];
			out = new FileOutputStream(new File(uploadedArchiveLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String output = "File uploaded to : " + uploadedArchiveLocation;
    	try {
			cleanSeinfeldData();
			sequenceFilesFromArchive(SEINFELD_ARCHIVE, PATH_IN_SEINFELD_SCRIPTS, 
					PATH_OUT_SEINFELD_SEQFILES, false);
			sparseVectorsFromSequenceFiles(PATH_OUT_SEINFELD_SEQFILES, PATH_OUT_SEINFELD_VECTORS, 
					"2", "4", "75", "TFIDF", "2", "org.apache.mahout.analysis.SeinfeldScriptAnalyzer");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return Response.status(200).entity(output).build();
	}
	
	public static void cleanSeinfeldData() throws FileNotFoundException {
		File dir = new File(".");
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File arg0) {
				return (arg0.isDirectory() && arg0.getName().startsWith(SEINFELD_PREFIX));
			}
		};
		File[] files = dir.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			deleteRecursive(files[i]);
		}
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
	
    public static void sequenceFilesFromGzipFile(File infile, boolean deleteOnSuccess) throws IOException
    {
    	  GZIPInputStream gin = new GZIPInputStream(new FileInputStream(infile));
    	  File outfile = new File(infile.getParent(), infile.getName().replaceAll("\\.gz$",""));
    	  FileOutputStream fos = new FileOutputStream(outfile);
    	  byte[] buf = new byte[100000]; 
    	  int len;
    	  while ( ( len = gin.read(buf) ) > 0 )
    	    fos.write(buf, 0, len);
    	  gin.close();
    	  fos.close();
    	  if (deleteOnSuccess)
    	    infile.delete();
    }
    
    public static void sequenceFilesFromArchive(File inFile, File extractTo, File seqFilePath, boolean deleteOnSuccess) throws Exception
    {
    	final TarGZipUnArchiver ua = new TarGZipUnArchiver();
    	ua.enableLogging(new ConsoleLogger( org.codehaus.plexus.logging.Logger.LEVEL_INFO, "console" ));
    	System.out.println("Extracting content of " + inFile.getCanonicalPath() + " to directory " + extractTo);
    	ua.setSourceFile(inFile);
    	extractTo.mkdirs();
    	ua.setDestDirectory(extractTo);
    	ua.extract();
    	
    	ToolRunner.run(new SequenceFilesFromDirectory(), new String[] {
    		"--input", extractTo.getCanonicalPath(),
    		"--output",seqFilePath.getCanonicalPath(),
    		"--charset","utf-8"
    	});
    }
    
    public static void sparseVectorsFromSequenceFiles(File input, File output,
    		String maxNGramSize, String minDF, String maxDF, 
    		String weight, String norm, String analyzerName) throws IOException, Exception
    {
    	ToolRunner.run(new SparseVectorsFromSequenceFiles(), new String [] {
				"--input", input.getCanonicalPath(),
				"--output", output.getCanonicalPath(),
                "--maxNGramSize", maxNGramSize,
                "--namedVector",
                "--minDF", minDF,
                "--maxDFPercent", maxDF,
                "--weight", weight,
                "--norm", norm,
                "--analyzerName", analyzerName
			});
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
    	try {
			cleanSeinfeldData();
			sequenceFilesFromArchive(SEINFELD_ARCHIVE, PATH_IN_SEINFELD_SCRIPTS, 
					PATH_OUT_SEINFELD_SEQFILES, false);
			sparseVectorsFromSequenceFiles(PATH_OUT_SEINFELD_SEQFILES, PATH_OUT_SEINFELD_VECTORS, 
					"2", "4", "75", "TFIDF", "2", "org.apache.mahout.analysis.SeinfeldScriptAnalyzer");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
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
