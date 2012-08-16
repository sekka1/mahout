<?php
/*
* This class performs actions on S3 datastore
*/

//if (!class_exists('S3')) require_once '../public/src/S3.php';
require 'S3.php';

// AWS access info
if (!defined('awsAccessKey')) define('awsAccessKey', 'AKIAJO6OOIFG3LCMZPGA');
if (!defined('awsSecretKey')) define('awsSecretKey', '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');

// Bucket name for this app
if (!defined('bucket')) define('bucket', 'algorithms.io');

class S3Usage{

	var $s3;

	public function __construct( ){

                $useSSL = false;

		// Instantiate the class
		$this->s3 = new S3(awsAccessKey, awsSecretKey, $useSSL);

	}
	public function test(){

		// List your buckets:
		echo "S3::listBuckets(): ".print_r($this->s3->listBuckets(), 1)."\n";
	}
	public function upload( $uploadFile ){
		// Input: Full system path to the file

		$uploadedFileName = '';

                if ( $this->s3->putObjectFile($uploadFile, bucket, baseName($uploadFile), S3::ACL_PRIVATE)) {
                    // ACL_PUBLIC_READ

			$uploadedFileName = baseName($uploadFile);
		}

		return $uploadedFileName; 
	}
        public function uploadText( $fileName, $text ){
            // Input: $fileName - a file name
            //          $text - a text string of what you want put onto S3

            // Create file and place onto the file system 
            $tmp_filename = '/tmp/'.md5( mt_rand( 1000, mt_getrandmax() ) . time() );
            $fp = fopen($tmp_filename, 'w');    
            fwrite( $fp, $text );
            fclose( $fp );
        
            // Upload to S3
            $uploadedFileName = '';

            if ( $this->s3->putObjectFile($tmp_filename, bucket, $fileName, S3::ACL_PRIVATE)) {
                // ACL_PUBLIC_READ

                $uploadedFileName = $fileName;
            }

            unlink( $tmp_filename );

            return $uploadedFileName;

        }
	public function getObjectInfo( $uploadFile ){ 
	// Input: Just the file name

		return $this->s3->getObjectInfo( bucket, baseName($uploadFile));
	}
        public function getBucket( $bucketName ){

            $returnVal = array();

            if (($contents = $this->s3->getBucket($bucketName)) !== false) {
                $returnVal = $contents;
            }
            return $returnVal;
        }
        public function getTextObjectandReturnContents( $uri ){
            // Gets a text object and returns the contents inside this file

            $returnVal = '';

            // To save it to a file (unbuffered write stream):
            //if (($object = S3::getObject( bucket, $uri, "/tmp/".$uri)) !== false) {
            //    print_r($object);
            //    var_dump($object);
            //}

            $file = '/tmp/'.$uri;

            // To write it to a resource (unbuffered write stream):
            $fp = fopen( $file, "wb" );
            if (($object = S3::getObject(bucket, $uri, $fp)) !== false) {
                //print_r($object);
                //var_dump($object);
                
                // open back up this file and read the contents and return it
                $handle = fopen($file, "rb");
                $contents = fread($handle, filesize($file));
                fclose( $handle );

                // Delte this file
                unlink( $file );

                $returnVal = $contents;
            }
            return $returnVal;
        }
}

?>
