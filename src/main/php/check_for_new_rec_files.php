<?php
include_once( 'S3Usage.php' );

$file_path = '/opt/Data-Sets/Automation/';

$s3 = new S3Usage();

// Get bucket list
$bucketList = $s3->getBucket( 'algorithms.io' );

date_default_timezone_set('America/Los_Angeles');

// Get the status file
$status_json = $s3->getTextObjectandReturnContents( 'Statuses.txt' );

foreach( $bucketList as $aFile ){

    if( preg_match( '/^rec_/', $aFile['name'] ) ){
        // Found some rec files

        if(file_exists($file_path.$aFile['name'])){

                // Check MD5 sum of file to see if it matches what is in S3
                if(md5_file($file_path.$aFile['name']) != $aFile['hash']){

                        // File has changed.  Download it again
                        downloadFile($s3, $file_path, $aFile);
                }
        }else{

                // File does not exist.  Download it.
                downloadFile($s3, $file_path, $aFile);
        }


    }
}
function downloadFile($s3Object, $file_path, $aFile){
//echo "DOWNLOADING ".$aFile['name']."\n";
        // Get the file
        $temp_file = $s3Object->getTextObjectandReturnContents( $aFile['name'] );

        // Out put file 
        $fp = fopen( $file_path.$aFile['name'], 'w' );
        fwrite( $fp, $temp_file );
        fclose( $fp );
}
?>
