## K-Means Clustering
### Overview
Clustering is the process of organizing a set of items into subsets (called clusters) so that items in the same cluster are similar. The similarity between items can be defined by a function or a formula, based on the context. For example, the Euclidean distance between two points acts as a similarity function for list of points/co-ordinates in space. Clustering is a method of unsupervised learning and a common technique for statistical data analysis used in many fields. The term clustering can also refer to automatic classification, numerical taxonomy, topological analysis etc. For more information on Clustering, see <http://en.wikipedia.org/wiki/Cluster_analysis>.

K-means is a generic clustering algorithm that can be applied easily to many situations.  It’s can also readily be executed on parallel computers.

### Getting Started

#### Seinfeld Episodes Example

Here’s an example on how to cluster Seinfeld episodes created by Frank Scholten @ Trifork for this example and you can find more [details](http://blog.trifork.nl/2011/04/04/how-to-cluster-seinfeld-episodes-with-mahout/) on how to execute this example straight from local machine.

1. Create a tar archive of the input files to cluster
	
		tar -cvzf archive.tgz <inputdir>
	
	To save some time, let's download a sample [archive](https://s3.amazonaws.com/sample_dataset.algorithms.io/seinfeld-scripts-preprocessed.tar.gz).  Each file in this archive is the entire script for one episode of Seinfeld.

2. [Upload](https://www.mashape.com/algorithms-io/algorithms-io#endpoint-Upload) the archive to algorithms.io.  Once uploaded, you will see a response that looks like this

		{
			"api": {
    		"Authentication": "Success"
    		},
    		"data": 3324
		}

3. Run clustering using [k-means endpoint](https://www.mashape.com/algorithms-io/algorithms-io#endpoint-K-Means-Clustering) or do that using a this curl command

		curl --include --request POST 'https://algorithms.p.mashape.com/jobs/swagger/42' \
		--header 'X-Mashape-Authorization: <your Mashape header here>' \
		-d 'method=sync' \
		-d 'ouputType=json' \
		-d 'datasource=3324' \
		-d 'analyzerName=io.algorithms.clustering.SeinfeldScriptAnalyzer' \
		-d 'maxNGramSize=2' \
		-d 'maxDF=75' \
		-d 'minDF=4' \
		-d 'weight=TFIDF' \
		-d 'norm=2' \
		-d 'numWords=10' \
		-d 'maxIter=10' \
		-d 'numClusters=10' \
		-d 'distanceMeasure=org.apache.mahout.common.distance.SquaredEuclideanDistanceMeasure'



