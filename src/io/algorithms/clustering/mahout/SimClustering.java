package io.algorithms.clustering.mahout;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.clustering.dirichlet.UncommonDistributions;
import org.apache.mahout.clustering.kmeans.Cluster;
import org.apache.mahout.clustering.kmeans.KMeansClusterer;
import org.apache.mahout.common.distance.EuclideanDistanceMeasure;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

public class SimClustering {
	private static void generateSamples(List<Vector> vectors, int num,
			double mx, double my, double sd) {
			for (int i = 0; i < num; i++) {
			vectors.add(new DenseVector(
			new double[] {
			UncommonDistributions.rNorm(mx, sd),
			UncommonDistributions.rNorm(my, sd),
			UncommonDistributions.rNorm(my, sd)
			} //testing 3 dimentions
			));
			}
			}
	public String get( String input_file, String numOfClustersWanted )
    {
        String data = "[";

        try{
        	if ( input_file == null || numOfClustersWanted == null )
        		data += "<br/>Incorrect usage check parameters!<br/>";
        	
        	List<Vector> sampleData = new ArrayList<Vector>();
        	int k = Integer.parseInt(numOfClustersWanted);
        	File file = new File(input_file);
        	BufferedReader br = new BufferedReader(new FileReader(file));
        	String delimiter = " ";
        	String line;
			while((line = br.readLine()) != null) {
				String[] results = line.split(delimiter);
				if (results !=null)
				{
				data += "Dimentions: " + results.length;
				double [] parsed = new double[results.length + 1];
				for (int i = 0; i < results.length; i++)
					parsed[i] = Double.parseDouble(results[i]);
				
				sampleData.add( new DenseVector( parsed ) );
				}
				else
					data += "wierd";
			}
			data += "<br/>Done reading the data file<br/>";
			
			
        	
	            //String recsFile = input_file;
	            
	            //create the data model
	            //FileDataModel dataModel = new FileDataModel(new File(recsFile));
	            //List<Vector> sampleData = new ArrayList<Vector>();
				//generateSamples(sampleData, 400, 1, 1, 3);
				//generateSamples(sampleData, 300, 1, 0, 0.5);
				//generateSamples(sampleData, 300, 0, 2, 0.1);
				//int k = 3;
				List<Vector> randomPoints = RandomPointsUtil.chooseRandomPoints(
				sampleData, k);
				List<Cluster> clusters = new ArrayList<Cluster>();
				int clusterId = 0;
				for (Vector v : randomPoints) {
				clusters.add(new Cluster(v, clusterId++,
				new EuclideanDistanceMeasure()));
				}
				List<List<Cluster>> finalClusters
				= KMeansClusterer.clusterPoints(sampleData, clusters,
				new EuclideanDistanceMeasure(), 3, 0.01);
				for(Cluster cluster : finalClusters.get(
				finalClusters.size() - 1)) {
					data += "Cluster id: " + cluster.getId()
				+ " center: " +
				cluster.getCenter().asFormatString() + "<br/>";
				}
				data += "c";
				
        	}
    	catch(Exception e){
    			System.out.println("gkan error" + e.getMessage() );
    			data += "<br />Error: " + e.getMessage() + "<br />";
			}

        data += "]";

        return data;

    }
}
