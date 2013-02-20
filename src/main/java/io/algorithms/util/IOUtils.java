/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
*/
package io.algorithms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import org.apache.mahout.classifier.sgd.CsvRecordFactory;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * Utilities for reading and persisting stuff.
 * @author Rajiv 
 */
public final class IOUtils {
    public static final String API_DATASET_URL_SUFFIX = "/dataset/id/",
            CONTENT_TYPE = "Content-Type",
            CONTENT_TYPE_JSON = "application/json",
            TMP_FOLDER = "tmp",
            TMP_FOLDER_FALLBACK = "/tmp",
            AUTH_TOKEN = "authToken";
    private static final Map<String, Class<?>> TYPES = new HashMap<String, Class<?>>();
    private static final ClientConfig ALL_TRUSTING_CLIENT_CONFIG = new DefaultClientConfig();
    private static final Logger LOG = LoggerFactory.getLogger(IOUtils.class);
    
    static {
        TYPES.put("string", String.class);
        TYPES.put("number", Double.class);
        TYPES.put("integer", Integer.class);
        TYPES.put("map", Map.class);
        TYPES.put("list", List.class);
        TYPES.put("datasource", File.class);
        
        try {
            SSLContext allTrustingSSLContext = SSLContext.getInstance("TLS");
            allTrustingSSLContext.init(null, new TrustManager[]{new X509TrustManager(){
                public X509Certificate[] getAcceptedIssuers(){return null;}
                public void checkClientTrusted(X509Certificate[] certs, String authType){}
                public void checkServerTrusted(X509Certificate[] certs, String authType){}
            }}, new SecureRandom());
            ALL_TRUSTING_CLIENT_CONFIG.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(
                    new HostnameVerifier() { public boolean verify(String arg0, SSLSession arg1) { return true; } }, allTrustingSSLContext));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Creates a RecordFactory instance which can interpret CSV files.
     * @param columnNameToTypeMap Map whose keys are the column names (as defined in the header), and values are the type.
     * Currently accepted types are "numeric", "word", and "text"
     * @param targetColumnName name of the column that contains the target variable (the class)
     * @param targetCategories list of strings that the target variable can have
     * @return record factory instance.
     */
    public static CsvRecordFactory createRecordFactoryForInputFormat(final Map<String, String> columnNameToTypeMap,
            final String targetColumnName, final List<String> targetClasses) {
        CsvRecordFactory factory = new CsvRecordFactory(targetColumnName, columnNameToTypeMap);
        factory.defineTargetCategories(targetClasses);
        factory.includeBiasTerm(true);
        return factory;
    }
    
    /**
     * Downloads files from algorithms.io datastore using the API.
     * @param authToken
     * @param algoServer
     * @param dataSourceId
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static File downloadFileFromAPI(String authToken, String algoServer, String dataSourceId) throws JsonParseException, JsonMappingException, IOException {
        File tmp = new File(TMP_FOLDER);
        if (!(tmp.exists() && tmp.isDirectory() && tmp.canWrite() || tmp.mkdirs())) {
            LOG.warn("Cannot use [" + TMP_FOLDER + "]. Falling back to [" + TMP_FOLDER_FALLBACK + "]");
            tmp = new File(TMP_FOLDER_FALLBACK);
        }

        LOG.info("Requested dataset id [" + dataSourceId + "] from algoServer [" + algoServer + "]");
        File output = new File(tmp, algoServer.replace("/", "") + ":" + authToken + ":" + dataSourceId);
        if (output.exists()) { // TODO: Assumes that the dataset never changes. Need to verify checksum.
            LOG.info("Dataset [" + dataSourceId + "] has already been downloaded to local filesystem.");
            return output;
        } 

        ClientResponse response = Client.create(ALL_TRUSTING_CLIENT_CONFIG) // TODO: HIGHLY UNSAFE
            .resource(algoServer + API_DATASET_URL_SUFFIX + dataSourceId)
            .accept(MediaType.WILDCARD)
            .header(AUTH_TOKEN, authToken)
            .get(ClientResponse.class);
        if (response.getClientResponseStatus().equals(Status.OK)) {
            ByteStreams.copy(response.getEntityInputStream(), new FileOutputStream(output));
            LOG.info("Successfully downloaded dataset id [" + dataSourceId + "] to [" + output.getAbsolutePath() + "]");
            return output;
        } else {
            throw new IOException("Received HTTP " + response.getClientResponseStatus() + " from " + algoServer);
        }
    }
    
    
    public static Class<?> getClazz(String type) {
        if (type == null) { type = "string"; }
        return TYPES.get(type);
    }
    
    /**
     * Returns the sha-1 checksum of the file
     * @param file
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String getSha1HashAsHexString(File file) throws NoSuchAlgorithmException, IOException {
        if (file == null) return null;
        return getSha1HashAsHexString(new FileInputStream(file));
    }
    
    /**
     * Returns the sha-1 checksum of the inputstream as a hex string
     * @param inputStream
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String getSha1HashAsHexString(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        if (inputStream == null) return null;
        
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] dataBytes = new byte[1024];
     
        int nread = 0; 
     
        try {
            while ((nread = inputStream.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        } finally {
            inputStream.close();
        }
        
        byte[] mdbytes = md.digest();
     
        //convert the byte to hex format
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
//    
//    private static final class RajivsSuperCSVRecordFactory implements RecordFactory {
//        final ConcurrentMap<String, Integer> dict = new ConcurrentHashMap<String, Integer>();
//        final String targetColumnName;
//        final Map<String, String> columnNameToTypeMap;
//        List<String> targetCategories;
//        int maxTargetValue;
//        String[] columnNames;
//        
//        /**
//         * @param targetColumnName
//         * @param columnNameToTypeMap
//         */
//        public RajivsSuperCSVRecordFactory(String targetColumnName,
//                Map<String, String> columnNameToTypeMap) {
//            this.targetColumnName = targetColumnName;
//            this.columnNameToTypeMap = columnNameToTypeMap;
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.mahout.classifier.sgd.RecordFactory#defineTargetCategories(java.util.List)
//         */
//        @Override
//        public void defineTargetCategories(List<String> list) {
//            this.targetCategories = list;
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.mahout.classifier.sgd.RecordFactory#maxTargetValue(int)
//         */
//        @Override
//        public RecordFactory maxTargetValue(int i) {
//            this.maxTargetValue = i;
//            return this;
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.mahout.classifier.sgd.RecordFactory#usesFirstLineAsSchema()
//         */
//        @Override
//        public boolean usesFirstLineAsSchema() {
//            return true;
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.mahout.classifier.sgd.RecordFactory#processLine(java.lang.String, org.apache.mahout.math.Vector)
//         */
//        @Override
//        public int processLine(String s, Vector vector) {
//            Preconditions.checkNotNull(s);
//            System.out.println(s);
//            Preconditions.checkNotNull(vector);
//            Preconditions.checkArgument(vector.size() == columnNames.length);
//            s = s.replace("\\\"", "");
//            CSVReader<String[]> reader = new CSVReaderBuilder<String[]>(new StringReader(s)).strategy(CSVStrategy.UK_DEFAULT).entryParser(new DefaultCSVEntryParser()).build();
// 
//            int output = -1;
//
//            try {
//                String[] values = reader.readNext();
//                Preconditions.checkArgument(values.length == columnNames.length);
//                // TODO: Fix. Target may not be the last column
//                vector.set(values.length - 1, 1);
//                for (int index = 0; index < values.length; index++) {
//                    String value = values[index];
//                    String columnName = columnNames[index];
//                    String type = columnNameToTypeMap.get(columnName);
//                    double valueDouble = 0;
//                    if (type.equals("word")) {
//                        synchronized (dict) {
//                            if (dict.containsKey(value)) {
//                                valueDouble = dict.get(value);
//                            } else {
//                                dict.put(value, dict.size());
//                                valueDouble = dict.size();
//                            }
//                        }
//                    } else if (type.equals("numeric")) {
//                        valueDouble = value.isEmpty() ? 0 : Double.parseDouble(value.replaceAll(",", ""));
//                    }
//                    boolean target = columnName.equals(targetColumnName);
//                    if (!target) {
//                        vector.set(index, valueDouble);
//                    } else if (targetCategories.contains(value)){
//                        return targetCategories.indexOf(value);
//                    }
//                }
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return output;
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.mahout.classifier.sgd.RecordFactory#getPredictors()
//         */
//        @Override
//        public Iterable<String> getPredictors() {
//            return Arrays.asList(columnNames);
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.mahout.classifier.sgd.RecordFactory#getTraceDictionary()
//         */
//        @Override
//        public Map<String, Set<Integer>> getTraceDictionary() {
//            return null;
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.mahout.classifier.sgd.RecordFactory#includeBiasTerm(boolean)
//         */
//        @Override
//        public RecordFactory includeBiasTerm(boolean flag) {
//            return this;
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.mahout.classifier.sgd.RecordFactory#getTargetCategories()
//         */
//        @Override
//        public List<String> getTargetCategories() {
//            return targetCategories;
//        }
//
//        /* (non-Javadoc)
//         * @see org.apache.mahout.classifier.sgd.RecordFactory#firstLine(java.lang.String)
//         */
//        @Override
//        public void firstLine(String s) {
//            CSVReader<String[]> reader = new CSVReaderBuilder<String[]>(new StringReader(s)).strategy(CSVStrategy.UK_DEFAULT).entryParser(new DefaultCSVEntryParser()).build();
//            try {
//                columnNames = reader.readNext();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
