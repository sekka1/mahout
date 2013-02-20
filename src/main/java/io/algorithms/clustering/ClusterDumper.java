package io.algorithms.clustering;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.mahout.clustering.Cluster;
import org.apache.mahout.clustering.classify.WeightedVectorWritable;
import org.apache.mahout.common.AbstractJob;
import org.apache.mahout.common.Pair;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.utils.vectors.VectorHelper;
import org.apache.mahout.clustering.iterator.ClusterWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public final class ClusterDumper extends AbstractJob {

  public static final String OUTPUT_OPTION = "output";

  public static final String DICTIONARY_TYPE_OPTION = "dictionaryType";

  public static final String DICTIONARY_OPTION = "dictionary";

  public static final String POINTS_DIR_OPTION = "pointsDir";

  public static final String JSON_OPTION = "json";

  public static final String NUM_WORDS_OPTION = "numWords";

  public static final String SUBSTRING_OPTION = "substring";

  public static final String SEQ_FILE_DIR_OPTION = "seqFileDir";

  private static final Logger log = LoggerFactory.getLogger(ClusterDumper.class);

  private Path seqFileDir;

  private Path pointsDir;

  private String termDictionary;

  private String dictionaryFormat;

  private String outputFile;

  private int subString = Integer.MAX_VALUE;

  private int numTopFeatures = 10;

  private Map<Integer, List<WeightedVectorWritable>> clusterIdToPoints;

  private boolean useJSON;

  public ClusterDumper(Path seqFileDir, Path pointsDir) throws IOException {
    this.seqFileDir = seqFileDir;
    this.pointsDir = pointsDir;
    init();
  }

  public ClusterDumper() {
    setConf(new Configuration());
  }

  public static void main(String[] args) throws Exception {
    new ClusterDumper().run(args);
  }

  public int run(String[] args) throws Exception {
    addOption(SEQ_FILE_DIR_OPTION, "s", "The directory containing Sequence Files for the Clusters", true);
    addOption(OUTPUT_OPTION, "o", "Optional output directory. Default is to output to the console.");
    addOption(SUBSTRING_OPTION, "b", "The number of chars of the asFormatString() to print");
    addOption(NUM_WORDS_OPTION, "n", "The number of top terms to print");
    addOption(JSON_OPTION, "j",
            "Output the centroid as JSON.  Otherwise it substitues in the terms for vector cell entries");
    addOption(POINTS_DIR_OPTION, "p",
            "The directory containing points sequence files mapping input vectors to their cluster.  "
                    + "If specified, then the program will output the points associated with a cluster");
    addOption(DICTIONARY_OPTION, "d", "The dictionary file");
    addOption(DICTIONARY_TYPE_OPTION, "dt", "The dictionary file type (text|sequencefile)", "text");
    if (parseArguments(args) == null) {
      return -1;
    }

    seqFileDir = new Path(getOption(SEQ_FILE_DIR_OPTION));
    if (hasOption(POINTS_DIR_OPTION)) {
      pointsDir = new Path(getOption(POINTS_DIR_OPTION));
    }
    outputFile = getOption(OUTPUT_OPTION);
    if (hasOption(SUBSTRING_OPTION)) {
      int sub = Integer.parseInt(getOption(SUBSTRING_OPTION));
      if (sub >= 0) {
        subString = sub;
      }
    }
    if (hasOption(JSON_OPTION)) {
      useJSON = true;
    }
    termDictionary = getOption(DICTIONARY_OPTION);
    dictionaryFormat = getOption(DICTIONARY_TYPE_OPTION);
    if (hasOption(NUM_WORDS_OPTION)) {
      numTopFeatures = Integer.parseInt(getOption(NUM_WORDS_OPTION));
    }
    init();
    printClusters(null);
    return 0;
  }

  public void printClusters(String[] dictionary) throws IOException, InstantiationException, IllegalAccessException {
    Configuration conf = new Configuration();

    if (this.termDictionary != null) {
      if ("text".equals(dictionaryFormat)) {
        dictionary = VectorHelper.loadTermDictionary(new File(this.termDictionary));
      } else if ("sequencefile".equals(dictionaryFormat)) {
          dictionary = VectorHelper.loadTermDictionary(conf, this.termDictionary);
      } else {
        throw new IllegalArgumentException("Invalid dictionary format");
      }
    }

    Writer writer;
    if (this.outputFile == null) {
      writer = new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
    } else {
      writer = new OutputStreamWriter(new FileOutputStream(new File(this.outputFile)), Charset.forName("UTF-8"));
    }
    try {
      FileSystem fs = seqFileDir.getFileSystem(conf);
      for (FileStatus seqFile : fs.globStatus(new Path(seqFileDir, "part-*"))) {
        Path path = seqFile.getPath();
        //System.out.println("Input Path: " + path); doesn't this interfere with output?
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
        
        try {
          Writable key = reader.getKeyClass().asSubclass(Writable.class).newInstance();
          Writable value = reader.getValueClass().asSubclass(Writable.class).newInstance();
          writer.write("{");
          boolean first = true;
          while (reader.next(key, value)) {
        	  if (!first) {
        		  writer.write(",");
        	  }
        	  else {
        		  first = false;
        	  }
        	  writer.write("\"");
        	  Cluster cluster = ((ClusterWritable) value).getValue();
        	  writer.write("C-");
        	  writer.write(String.valueOf(cluster.getId()));
        	  writer.write("\":{");

	            if (dictionary != null) {
	              String topTerms = getTopFeatures(cluster.getCenter(), dictionary, numTopFeatures);
	              writer.write("\"top features\":");
	              writer.write(topTerms);
	              writer.write(",");
	            }

            List<WeightedVectorWritable> points = clusterIdToPoints.get(cluster.getId());
            if (points != null) {
                writer.write("\"named vectors\":[");
                Iterator pointiter = points.iterator();
                while (pointiter.hasNext()) {
                	writer.write("\"");
                	NamedVector namedVector = (NamedVector) ((WeightedVectorWritable)pointiter.next()).getVector();
                	writer.write(namedVector.getName());
                	writer.write("\"");
                	if (pointiter.hasNext()) {
                		writer.write(",");
                	}	
                }
                writer.write("]");
            }
            writer.write("}");
          }
          writer.write("}");
        } finally {
          reader.close();
        }
      }
    } finally {
      writer.close();
    }
  }

  private void init() throws IOException {
    if (this.pointsDir != null) {
      Configuration conf = new Configuration();
      // read in the points
      clusterIdToPoints = readPoints(this.pointsDir, conf);
    } else {
      clusterIdToPoints = Collections.emptyMap();
    }
  }

  public void setOutputFile(String outputFile) {
    this.outputFile = outputFile;
  }

  public int getSubString() {
    return subString;
  }

  public void setSubString(int subString) {
    this.subString = subString;
  }

  public Map<Integer, List<WeightedVectorWritable>> getClusterIdToPoints() {
    return clusterIdToPoints;
  }

  public String getTermDictionary() {
    return termDictionary;
  }

  public void setTermDictionary(String termDictionary, String dictionaryType) {
    this.termDictionary = termDictionary;
    this.dictionaryFormat = dictionaryType;
  }

  public void setNumTopFeatures(int num) {
    this.numTopFeatures = num;
  }

  public int getNumTopFeatures() {
    return this.numTopFeatures;
  }

  public static Map<Integer, List<WeightedVectorWritable>> readPoints(Path pointsPathDir,
                                                                      Configuration conf) throws IOException {
    Map<Integer, List<WeightedVectorWritable>> result = new TreeMap<Integer, List<WeightedVectorWritable>>();

    FileSystem fs = pointsPathDir.getFileSystem(conf);
    FileStatus[] children = fs.listStatus(pointsPathDir, new PathFilter() {
      public boolean accept(Path path) {
        String name = path.getName();
        return !(name.endsWith(".crc") || name.startsWith("_"));
      }
    });

    for (FileStatus file : children) {
      Path path = file.getPath();
      SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);
      try {
        IntWritable key = reader.getKeyClass().asSubclass(IntWritable.class).newInstance();
        WeightedVectorWritable value = reader.getValueClass().asSubclass(WeightedVectorWritable.class).newInstance();
        while (reader.next(key, value)) {
          // value is the cluster id as an int, key is the name/id of the
          // vector, but that doesn't matter because we only care about printing
          // it
          //String clusterId = value.toString();
          List<WeightedVectorWritable> pointList = result.get(key.get());
          if (pointList == null) {
            pointList = new ArrayList<WeightedVectorWritable>();
            result.put(key.get(), pointList);
          }
          pointList.add(value);
          value = reader.getValueClass().asSubclass(WeightedVectorWritable.class).newInstance();
        }
      } catch (InstantiationException e) {
        log.error("Exception", e);
      } catch (IllegalAccessException e) {
        log.error("Exception", e);
      }
    }

    return result;
  }

  static class TermIndexWeight {
    private int index = -1;

    private final double weight;

    TermIndexWeight(int index, double weight) {
      this.index = index;
      this.weight = weight;
    }
  }

  public static String getTopFeatures(Vector vector, String[] dictionary, int numTerms) {

    List<TermIndexWeight> vectorTerms = new ArrayList<TermIndexWeight>();

    Iterator<Vector.Element> iter = vector.iterateNonZero();
    while (iter.hasNext()) {
      Vector.Element elt = iter.next();
      vectorTerms.add(new TermIndexWeight(elt.index(), elt.get()));
    }

    // Sort results in reverse order (ie weight in descending order)
    Collections.sort(vectorTerms, new Comparator<TermIndexWeight>() {
      public int compare(TermIndexWeight one, TermIndexWeight two) {
        return Double.compare(two.weight, one.weight);
      }
    });

    Collection<Pair<String, Double>> topTerms = new LinkedList<Pair<String, Double>>();

    for (int i = 0; (i < vectorTerms.size()) && (i < numTerms); i++) {
      int index = vectorTerms.get(i).index;
      String dictTerm = dictionary[index];
      if (dictTerm == null) {
        log.error("Dictionary entry missing for {}", index);
        continue;
      }
      topTerms.add(new Pair<String, Double>(dictTerm, vectorTerms.get(i).weight));
    }

    StringBuilder sb = new StringBuilder(100);
    sb.append("[\"");

    Iterator<Pair<String, Double>> iterator = topTerms.iterator();
    while (iterator.hasNext()) {
      Pair<String, Double> item = iterator.next();
      String term = item.getFirst();
      sb.append(StringUtils.capitalize(term));
      if (iterator.hasNext()) {
        sb.append("\", \"");
      }
//      sb.append(StringUtils.rightPad(term, 40));
//      sb.append("=>");
//
    }
    sb.append("\"]");
    return sb.toString();
  }

}
