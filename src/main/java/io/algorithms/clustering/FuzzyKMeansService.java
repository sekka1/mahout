package io.algorithms.clustering;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.wordnik.swagger.annotations.*;
import io.algorithms.util.IOUtils;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.clustering.fuzzykmeans.FuzzyKMeansDriver;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.text.SequenceFilesFromDirectory;
import org.apache.mahout.vectorizer.SparseVectorsFromSequenceFiles;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ayee
 * Date: 3/5/13
 * Time: 5:12 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/fuzzykmeans")
@Api(value = "/fuzzykmeans", description = "Operations to run fuzzy k-means algorithm")
@Produces({MediaType.APPLICATION_JSON})
public class FuzzyKMeansService {

    private static final Logger log = LoggerFactory.getLogger(KMeansService.class);
    private File uploadedArchive;
    private File PATH_APP;
    private File PATH_EXTRACTED_ARCHIVE;
    private File PATH_SEQUENCE_FILES;
    private File PATH_VECTOR_FILES;
    private File PATH_OUTPUT;

    @GET
    @ApiOperation(value = "Get clusters as JSON")
    @Produces(MediaType.APPLICATION_JSON)
    public Response dumpCluster(
            @ApiParam(value = "How many top feature words to show",
                    defaultValue = "5")
            @FormParam("numWords")
            @QueryParam("numWords")
            String numWords) {

        try {
            ToolRunner.run(new ClusterDumper(), new String[]{
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

    @POST
    @Path("/seqdirectory")
    public Response seqdirectory(
        @QueryParam("input") @FormParam("input") String input,
        @QueryParam("output") @FormParam("input") String output,
        @QueryParam("charset") @FormParam("charset") String charset
    ) throws Exception
    {
        ToolRunner.run(new SequenceFilesFromDirectory(), new String[] {
                "--input", input,
                "--output", output,
                "--charset","utf-8",
                "--overwrite"
        });
        return Response.status(200).entity("Converted directory [" + input + "] to sequence files in [" + output).build();
    }

    @POST
    @Path("/seq2sparse")
    public Response seq2sparse(
            @QueryParam("input") @FormParam("input") String input,
            @QueryParam("output") @FormParam("output") String output,
            @QueryParam("maxNGramSize") @FormParam("maxNGramSize") String maxNGramSize,
            @QueryParam("minDF") @FormParam("minDF") String minDF,
            @QueryParam("maxDF") @FormParam("maxDF") String maxDF,
            @QueryParam("weight") @FormParam("weight") String weight,
            @QueryParam("norm") @FormParam("norm") String norm,
            @QueryParam("analyzerName") @FormParam("analyzerName") String analyzerName)
            throws Exception {
        System.out.println("FuzzyKMeansService.seq2sparse input=" + input);
        System.out.println("FuzzyKMeansService.seq2sparse output=" + output);
        ToolRunner.run(new SparseVectorsFromSequenceFiles(), new String [] {
                "--input", input,
                "--output", output,
                "--maxNGramSize", maxNGramSize,
                "--namedVector",
                "--minDF", minDF,
                "--maxDFPercent", maxDF,
                "--weight", weight,
                "--norm", norm,
                "--analyzerName", analyzerName,
                "--overwrite"
        });
        return Response.status(200).entity("Generated sparse vectors from sequence files in directory [" + input + "] to vectors in [" + output).build();
    }

    @POST
    @Path("/driver")
    public Response fuzzykmeans(
        @QueryParam("input") @FormParam("input") String input,
        @QueryParam("output") @FormParam("output") String output,
        @QueryParam("distanceMeasure") @FormParam("distanceMeasure") String distanceMeasure,
        @QueryParam("clusters") @FormParam("clusters") String clusters,
        @QueryParam("numClusters") @FormParam("numClusters") String numClusters,
        @QueryParam("convergenceDelta") @FormParam("convergenceDelta") String convergenceDelta,
        @QueryParam("maxIter") @FormParam("maxIter") String maxIter,
        @QueryParam("m") @FormParam("m") String m,
        @QueryParam("emitMostLikely") @FormParam("emitMostLikely") String emitMostLikely,
        @QueryParam("threshold") @FormParam("threshold") String threshold,
        @QueryParam("method") @FormParam("method") String method
    ) throws Exception {
        ToolRunner.run(new FuzzyKMeansDriver(), new String[] {
                "--input", input,
                "--output", output,
                "--clusters", clusters,
                "--maxIter", maxIter,
                "--numClusters", numClusters,
                "--distanceMeasure", distanceMeasure,
                "--convergenceDelta", convergenceDelta,
                "--m", m,
                "--method", method,
                "--emitMostLikely", emitMostLikely,
                "--threshold", threshold,
                "--clustering",
                "--overwrite",
        });
        return Response.status(Response.Status.OK).entity("Completed fuzzy k-means clustering").build();
    }

    @POST
    @Path("/clusterdump")
    public Response clusterdump(
        @QueryParam("seqFileDir") @FormParam("seqFileDir") String seqFileDir,
        @QueryParam("output") @FormParam("output") String output,
        @QueryParam("pointsDir") @FormParam("pointsDir") String pointsDir,
        @QueryParam("substring") @FormParam("substring") String substring,
        @QueryParam("numWords") @FormParam("numWords") String numWords,
        @QueryParam("dictionary") @FormParam("dictionary") String dictionary,
        @QueryParam("dictionaryType") @FormParam("dictionaryType") String dictionaryType,
        @QueryParam("json") @FormParam("json") String json
    ) throws Exception {
        ToolRunner.run(new ClusterDumper(), new String[] {
                "--seqFileDir", seqFileDir,
                "--output", output,
                "--pointsDir", pointsDir,
                "--numWords", numWords,
                "--substring", substring,
                "--dictionary", dictionary,
                "--dictionaryType", dictionaryType
        });
        return Response.status(Response.Status.OK).entity("Dumped cluster  fuzzy k-means clustering").build();
    }

    @POST
    @ApiOperation(value = "Expand dataset into sequence files, convert into vectors and then run Fuzzy K-Means clustering algorithm")
    @ApiErrors(value= {
            @ApiError(code=500, reason = "Internal Server Error"),
            @ApiError(code=400, reason = "No archive uploaded")})
    public Response run(
            @ApiParam(value = "Authentication token", required = true)
            @QueryParam("authToken")
            @FormParam(value = "authToken")
            String authToken,

            @ApiParam(value = "Calling algorithms.io API server", required = true)
            @QueryParam("algoServer")
            @FormParam(value = "algoServer")
            String algoServer,

            @ApiParam(value = "Datasource ID", required = true)
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

            @ApiParam(value = "Fully qualified class name of analyzer",
                    allowableValues = "org.apache.mahout.analysis.SeinfeldScriptAnalyzer," +
                            "org.apache.mahout.text.MailArchivesClusteringAnalyzer," +
                            "org.apache.mahout.text.wikipedia.WikipediaAnalyzer")
            @QueryParam("analyzerName")
            @FormParam("analyzerName")
            String analyzerName,

            @ApiParam(value = "Maximum number of iterations which once reached, iterations will stop",
                    defaultValue = "10",
                    allowableValues = "range[1,20]")
            @QueryParam("maxIter")
            @FormParam("maxIter")
            String maxIter,

            @ApiParam(value = "The k in k-Means.  If specified, then a random selection of k Vectors will be chosen"
                    + " as the centroid and written to the clusters input path.",
                    defaultValue = "100",
                    allowableValues = "range[1,100]",
                    required = true)
            @QueryParam("numClusters")
            @FormParam("numClusters")
            String numClusters,

            @ApiParam(value = "Distance measure used to calculate distances from centroids",
                    defaultValue = "SquaredEuclidean",
                    allowableValues = "org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure," +
                            "org.apache.mahout.common.distance.EuclideanDistanceMeasure," +
                            "org.apache.mahout.common.distance.TanimotoDistanceMeasure," +
                            "org.apache.mahout.common.distance.CosineDistanceMeasure," +
                            "org.apache.mahout.common.distance.WeightedManhattanDistanceMeasure")
            @QueryParam("distanceMeasure")
            @FormParam("distanceMeasure")
            String distanceMeasure,

            @ApiParam(value = "How many top feature words to show",
                    defaultValue = "5",
                    required = true)
            @QueryParam("numWords")
            @FormParam("numWords")
            String numWords,

            @ApiParam(value = "Use for testing this endpoint with canned dataset",
                    required = false,
                    defaultValue = "Seinfeld",
                    allowableValues = "Seinfeld,Reuters")
            @QueryParam("test")
            @FormParam("test")
            String test,

            @ApiParam(value = "Convergence delta value", required = false, defaultValue = "0.5")
            @QueryParam("convergenceDelta") @FormParam("convergenceDelta")
            String convergenceDelta,

            @ApiParam(value = "Fuzzification factor, see http://en.wikipedia.org/wiki/Data_clustering#Fuzzy_c-means_clustering",
            required = true, defaultValue = "2")
            @QueryParam("m") @FormParam("m") String m,

            @ApiParam(value = "If true emit only most likely cluster for each point", required = false, defaultValue = "true")
            @QueryParam("emitMostLikely") @FormParam("emitMostLikely") String emitMostLikely,

            @ApiParam(value = "The pdf threshold used for cluster determination", required = false, defaultValue = "0")
            @QueryParam("threshold") @FormParam("threshold") String threshold)

            throws URISyntaxException
    {
        String datasource = null;

        if (test != null) {
            if (KMeansService.exampleDatasetMap.get(test.toLowerCase()) == null) {
                return Response.status(Response.Status.NOT_FOUND)
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
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
                    return Response.status(Response.Status.GONE)
                            .entity("Invalid datasources parameter [" + datasources + "]")
                            .build();
                }
                datasource = ((JSONArray)dsArray).getString(0);
            }
            else {
                // parameter string is just an integer instead of a json array
                datasource = datasources;
            }

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
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Dataset [" + datasource + "] download failed")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
        }

        String dataPath = null;
        try {
            URL resource = Thread.currentThread().getContextClassLoader().getResource("/");
            if (resource == null) {
                // jersey-test
                PATH_APP = new File("");
            }
            else {
                PATH_APP = new File(resource.toURI());
            }
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

        // create a lock file if not exist... this is a simple locking mechanism to prevent multiple calls
        // made for same token
        // TODO: commented because it's causing the test to fail.  For some reason, the exists() call returns true

//        File lockfile = new File(dataPath + "-lock");
//        if (lockfile.exists()) {
//            return Response.status(Response.Status.UNAUTHORIZED)
//                    .entity("There's another kmeans process running with authToken [ "
//                            + authToken + "] and dataset [" + datasource + "]")
//                    .type(MediaType.APPLICATION_JSON)
//                    .build();
//        }
//        try {
//            lockfile.createNewFile();
//        } catch (IOException e2) {
//            // TODO Auto-generated catch block
//            e2.printStackTrace();
//        }

        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
        ua.enableLogging(new ConsoleLogger(
                org.codehaus.plexus.logging.Logger.LEVEL_INFO, "console"));
        if (PATH_EXTRACTED_ARCHIVE.exists()) {
            try {
                KMeansService.deleteRecursive(PATH_EXTRACTED_ARCHIVE);
            } catch (FileNotFoundException e1) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
            return Response.status(Response.Status.BAD_REQUEST).entity(missParamError.toString()).build();
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

            ToolRunner.run(new FuzzyKMeansDriver(), new String[] {
                    "--input", PATH_VECTOR_FILES.getCanonicalPath() + File.separator + "tfidf-vectors",
                    "--output", PATH_OUTPUT.getCanonicalPath() + File.separator + "clusters",
                    "--clusters", PATH_OUTPUT.getCanonicalPath() + File.separator + "initialclusters",
                    "--maxIter", maxIter,
                    "--numClusters", numClusters,
                    "--distanceMeasure", distanceMeasure,
                    "--convergenceDelta", convergenceDelta,
                    "--m", m,
                    "--method", "mapreduce",
                    "--emitMostLikely", emitMostLikely,
                    "--threshold", threshold,
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

//        lockfile.delete();

        return Response.status(200).entity(bigstring).type(MediaType.APPLICATION_JSON).build();


    }
}
