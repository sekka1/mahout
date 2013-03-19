## Anomaly Detection
### Overview
Anomaly detection refers to detecting patterns in a given data set that do not conform to an established normal behavior.  Among the main techniques used, unsupervised anomaly detection ones detect anomalies in an unlabeled test data set under the assumption that the majority of the instances in the data set are normal by looking for instances that seem to fit least to the remainder of the data set.
In this tutorial, we will demonstrate how you can use Algorithms.IO to do that.

#### Suspicious Transaction

Here we will demonstrate how to use proximity analysis to identify suspicious entries out of a large set of purchase records, with the great help from Pranab Gnosh.
For design details of this algorithm, you can visit his blog [post](http://pkghosh.wordpress.com/2012/06/18/its-a-lonely-life-for-outliers/).

Let's get started

1. First let's take a look at the purchase records
	
		C5N5VRR6[]1224[]205.35[]super market
		FUUYJ3VU[]903[]11.40[]drug store
		2P4CP25Z[]1037[]46.0[]restaurant
		ARFH5ILQ[]226[]74.90[]clothing store
		7AYQPRY8[]57[]18.80[]grocery
		..
		..
		..
	
	To save some time, let's download a sample .  Each file in this archive is the entire script for one episode of Seinfeld.

2. Download this [sample dataset](https://s3.amazonaws.com/sample_dataset.algorithms.io/outlier-sample) and then [upload](https://www.mashape.com/algorithms-io/algorithms-io#endpoint-Upload) this  to algorithms.io.  Once uploaded, you will see a response that looks like this

		{
			"api": {
    		"Authentication": "Success"
    		},
    		"data": <input>
		}

3. Download this [configuration file](https://s3.amazonaws.com/sample_dataset.algorithms.io/outlier/cct.properties) and this [purchase schema](https://s3.amazonaws.com/sample_dataset.algorithms.io/outlier/prod.json).  [Upload](https://www.mashape.com/algorithms-io/algorithms-io#endpoint-Upload) these files in the same way to us.  Note down the data set references from response messages.

		{
			"api": {
    		"Authentication": "Success"
    		},
    		"data": <conf>
		}
		
		
		{
			"api": {
    		"Authentication": "Success"
    		},
    		"data": <schema>
		}
4. Now you are ready to rock!  Start the anomaly detection either by submitting [here](http://pod3.staging.www.algorithms.io/dashboard/algodoc/id/<algo-id>?category=/clustering) or if you prefer right use a curl command like this

		curl -i -d 'job_params={
    		"job": {
        		"algorithm": {
            		"id": "48",
            		"params": [
                		[
                    		"org.sifarish.feature.SameTypeSimilarity",
                    		"-Dconf.path=$datasource_<conf>",
                    		"-Dschema.path=$datasource_<schema>"
                    		"$input_path",
                    		"$output_path_1"
                		],
               			[
                    		"org.beymani.proximity.AverageDistance",
                    		"-Dconf.path=$datasource_<conf>",
                    		"-Dschema.path=$datasource_<schema>"
                    		"$output_path_1",
                    		"$output_path_final"
                		]
            		]
        		},
        		"action": "submit",
        		"datasources": [
            		"3483",
            		"3483"
        		],
        		"setup_params": {
            		"masterInstanceSize": "m1.small",
            		"slaveInstanceSize": "m1.small",
            		"instanceCount": "1"
        		}
    		}
		}' -H "authToken: <your token>" http://v1.api.algorithms.io/jobs
		
	Once submitted, you will get this message from the system with a job_id reference.

		{"job_id":"41ba3b2bbed9e08cf64e3b3dfaf3190c","status":"Running"} 
		
5. To check out if the job is complete, use this command with  


		curl -i -d 'job_params={"job":{"algorithm":{"id":"48","params":{"job_id":"41ba3b2bbed9e08cf64e3b3dfaf3190c"} },"action":"get_status"}}' -H "authToken: <auth token>" http://v1.api.algorithms.io/jobs -v

	Once the algorithm job run is complete, the status will show the reference of the output

		{
        	"status": {
				...
        	},
        	"final": {
            	"output": {
                	"datasource_id_seq": 3499
            	}
        	}
    	} 
6. Finally the output data will look like this.  Every purchase will be given an outlier score.  The higher it is, the more so it's identified as an outlier.

		12XY2V7R[]381
		19TQXUNX[]353
		1LFWDZKT[]340
		1NZGI7C9[]353


