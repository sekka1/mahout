package io.algorithms.clustering.mahout;

import io.algorithms.utils.MyLogger;

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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class SimClusteringOld {

	public boolean bJFreeChart; //if this is on then it will show the chart otherwise it wont make a chart
	public static final double[][] points = { {1, 1}, {2, 1}, {1, 2},
		{2, 2}, {3, 3}, {8, 8},
		{9, 8}, {8, 9}, {9, 9}};
	private static final int CHART_WIDTH = 640;
	private static final int CHART_HEIGHT = 640;
	
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
		
		public static List<Vector> getPointsFromFile( String filename, String delimiter ) {
			List<Vector> points = new ArrayList<Vector>();
			try
			{
				double[][] raw;
				
				BufferedReader br = new BufferedReader(new FileReader(filename));
				String line;
				MyLogger.getInstance().setLogFileName("feature.txt");
				while((line = br.readLine()) != null) {
					String[] results = line.split(delimiter);
					double [] parsed = {Double.parseDouble(results[0]),Double.parseDouble(results[1])};
					Vector vec = new RandomAccessSparseVector(parsed.length);
					vec.assign(parsed);
					points.add(vec);
					MyLogger.getInstance().log(line);
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
		private SecureRandom random = new SecureRandom();

		  public String nextSessionId()
		  {
		    return new BigInteger(130, random).toString(32);
		  }

		private static Random r = new Random();
		private static XYDataset createDataset(double[][] data ) {
		    XYSeriesCollection result = new XYSeriesCollection();
		    XYSeries series = new XYSeries("Mahout Data");
		    for (int i = 0; i < data.length; i++) {
		        double x = data[i][0];
		        double y = data[i][1];
		        series.add(x, y);
		    }
		    result.addSeries(series);
		    return result;
		}
		
		private static XYDataset createDataset(List<Vector> data ) {
		    XYSeriesCollection result = new XYSeriesCollection();
		    XYSeries series = new XYSeries("Mahout Data");
		    Iterator<Vector> itr = data.iterator();
		    while (itr.hasNext())
		    {
		    	Vector v = itr.next();
		        double x = v.get(0);
		        double y = v.get(1);
		        series.add(x, y);
		    }
		    result.addSeries(series);
		    return result;
		}
		
		private static XYDataset createRandomDataset(int numPoints) {
		    XYSeriesCollection result = new XYSeriesCollection();
		    XYSeries series = new XYSeries("Mahout Data");
		    for (int i = 0; i < numPoints; i++) {
		        double x = r.nextDouble() * 5;
		        double y = r.nextDouble() * 5;
		        series.add(x, y);
		    }
		    result.addSeries(series);
		    return result;
		}
		
		private void createClusterChart(List<Vector> data, ArrayList<Cluster> cluster_list, String filename )
		{
			try
		      {
				JFreeChart chart = ChartFactory.createScatterPlot(
			            "Scatter Plot", // chart title
			            "X", // x axis label
			            "Y", // y axis label
			            createDataset(data), // data  ***-----PROBLEM------***
			            PlotOrientation.VERTICAL,
			            true, // include legend
			            true, // tooltips
			            false // urls
			            );
				
				for (int i = 0; i < cluster_list.size(); i++)
				{
					addCircle(chart, cluster_list.get(i).getCenter().get(0), cluster_list.get(i).getCenter().get(1), 2.0, Color.red);
				}
		        saveChart(chart, filename);
				
				
		      }
		      catch (Exception e)
		      {
		         System.out.println("rob error: " + e.toString());
		      }
		}
		
		private void createClusterChart(double[][] data, ArrayList<Cluster> cluster_list, String filename ) throws IOException {
			try
		      {
				JFreeChart chart = ChartFactory.createScatterPlot(
			            "Scatter Plot", // chart title
			            "X", // x axis label
			            "Y", // y axis label
			            createDataset(data), // data  ***-----PROBLEM------***
			            PlotOrientation.VERTICAL,
			            true, // include legend
			            true, // tooltips
			            false // urls
			            );
				
				for (int i = 0; i < cluster_list.size(); i++)
				{
					addCircle(chart, cluster_list.get(i).getCenter().get(0), cluster_list.get(i).getCenter().get(1), 2.0, Color.red);
				}
		        saveChart(chart, filename);
				
				
		      }
		      catch (Exception e)
		      {
		         System.out.println("rob error: " + e.toString());
		      }

		   }
		
		//this can draw an ellipse too but I just want circles
		private void addCircle( JFreeChart chart, double x_center, double y_center, double r, Color c )
		{
			if (r < 0)
			{
				System.out.println("Radius less than 0");
				System.exit(1);
			}
			XYPlot plot = (XYPlot) chart.getPlot();
			Ellipse2D e = new Ellipse2D.Double(x_center, y_center, r, r);
	        plot.addAnnotation(new XYShapeAnnotation(e, new BasicStroke(1.0f), c));
		}
		private void saveChart( JFreeChart chart, String filename)
		{
			try {
				File file = new File(filename); 
				
				ChartUtilities.saveChartAsPNG(file,chart,400,300);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		@SuppressWarnings("null")
		public String get( String CoordinatePairFile, String numClusters ) {
			
			String data = "[";
			bJFreeChart = true;
	        try{
			int k = Integer.parseInt(numClusters);
			//List<Vector> vectors = getPoints(points);
			List<Vector> vectors = getPointsFromFile(CoordinatePairFile,",");
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
			
			//Create the chart here
			if (bJFreeChart){
			String filename = "scatter.png";// + nextSessionId() + ".png";
			createClusterChart(vectors, cList, filename);
			}
			
			
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
			}
			reader.close();
			
			}
			catch( Throwable e )
			{
				System.out.println("gkan error:" + e.getMessage());
				data += e.getMessage() + MyLogger.getStackTrace(e);
			}
	        
	        data += "]";
	        return data;
	        
		}
}
