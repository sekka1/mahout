package io.algorithms.clustering.mahout;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.mahout.clustering.WeightedVectorWritable;
import org.apache.mahout.clustering.kmeans.Cluster;
import org.apache.mahout.clustering.kmeans.KMeansDriver;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.VectorWritable;
public class SimClusteringParallel {

	public static final double[][] points = { {1, 1}, {2, 1}, {1, 2},
		{2, 2}, {3, 3}, {8, 8},
		{9, 8}, {8, 9}, {9, 9}};
	
		public static void writePointsToFile(List<Vector> points, String fileName,FileSystem fs,Configuration conf) throws IOException	{
			Path path = new Path(fileName);
			SequenceFile.Writer writer = new SequenceFile.Writer(fs, conf,
			path, LongWritable.class, VectorWritable.class);
			long recNum = 0;
			VectorWritable vec = new VectorWritable();
			for (Vector point : points) {
				vec.set(point);
				writer.append(new LongWritable(recNum++), vec);
			}
			writer.close();
		}
		
		public static List<Vector> getPointsFromFile( String filename ) {
			List<Vector> points = new ArrayList<Vector>();
			try
			{
				
				BufferedReader br = new BufferedReader(new FileReader(filename));
				String line;
				while((line = br.readLine()) != null) {
					List<String> digits = new ArrayList<String>();
					try {
					    Pattern regex = Pattern.compile("[+-]?\\d*\\.?\\d+");
					    Matcher regexMatcher = regex.matcher(line);
					    
					    
					    while (regexMatcher.find()) {
					       /* for (int i = 1; i <= regexMatcher.groupCount(); i++) {
					                System.out.println(regexMatcher.group(i));
					                // match start: regexMatcher.start(i)
					                // match end: regexMatcher.end(i)
					        }*/
					    	digits.add(regexMatcher.group(0));
					    } 
					    
					} catch (PatternSyntaxException ex) {
					    // Syntax error in the regular expression
						System.out.println(ex.getMessage());
					}
					
					//now to put in the digits into parsed
					
					double [] parsed = new double[digits.size() + 1];
					for (int i = 0; i < digits.size(); i++)
						parsed[i] = Double.parseDouble(digits.get(i));
					
					//double [] parsed = {Double.parseDouble(digits.get(0)),Double.parseDouble(digits.get(1))};
					//int i = 0;
					/*for (String s : digits)
					{
						parsed[i++] = Double.parseDouble(s);
					}
					*/
					System.out.println("digits size : " + digits.size());
					Vector vec = new RandomAccessSparseVector(parsed.length);
					vec.assign(parsed);
					points.add(vec);
				}
			}
			catch(Exception e)
			{
				System.out.println("robI error:" + e.getMessage());
			}
			System.out.println(points.toString());
			return points;
		}
		
		public static List<Vector> getPoints(double[][] raw) {
			List<Vector> points = new ArrayList<Vector>();
			for (int i = 0; i < raw.length; i++) 
			{
				double[] fr = raw[i];
				Vector vec = new RandomAccessSparseVector(fr.length);
				vec.assign(fr);
				points.add(vec);
			}
			return points;
		}
		
		
		@SuppressWarnings("null")
		public String get( String CoordinatePairFile, String numClusters ) {
			
			String data = "[";
	        try{
			int k = Integer.parseInt(numClusters);
			List<Vector> vectors = getPoints(points);
			//List<Vector> vectors = getPointsFromFile(CoordinatePairFile);
			File testData = new File("testdata");
			if (!testData.exists()) {
				testData.mkdir();
			}
			testData = new File("testdata/points");
			
			if (!testData.exists()) {
				testData.mkdir();
			}
			Configuration conf = new Configuration();
			FileSystem fs = FileSystem.get(conf);
			writePointsToFile(vectors,
			"testdata/points/file1", fs, conf);
			Path path = new Path("testdata/clusters/part-00000");
			SequenceFile.Writer writer
			= new SequenceFile.Writer(
			fs, conf, path, Text.class, Cluster.class);
			
			
			ArrayList<Cluster> cList = new ArrayList<Cluster>();
			for (int i = 0; i < k; i++) {
				Vector vec = vectors.get(i);
				Cluster cluster = new Cluster(
				vec, i, new EuclideanDistanceMeasure());
				if (cluster!=null)
				{
					cList.add( cluster );
					//data += "added a cluster";
				}
				writer.append(new Text(cluster.getIdentifier()), cluster);
			}
			
			
			writer.close();
			
			
			
			KMeansDriver.run(conf, new Path("testdata/points"),
			new Path("testdata/clusters"),
			new Path("output"), new EuclideanDistanceMeasure(),
			0.001, 10, true, false);
			SequenceFile.Reader reader
			= new SequenceFile.Reader(fs,
			new Path("output/" + Cluster.CLUSTERED_POINTS_DIR
			+ "/part-m-00000"), conf);
			
			IntWritable key = new IntWritable();
			WeightedVectorWritable value = new WeightedVectorWritable();
			
			while (reader.next(key, value)) {
			System.out.println(
			value.toString() + " belongs to cluster "
			+ key.toString());
			
			data += value.toString() + " belongs to cluster "
					+ key.toString();
			}
			reader.close();
			
			}
			catch( Throwable e )
			{
				System.out.println("gkan error:" + e.getMessage());
				data += e.getMessage();
			}
	        
	        data += "]";
	        return data;
	        
		}
}
