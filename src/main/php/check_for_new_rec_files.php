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

        // Get the file
        $temp_file = $s3->getTextObjectandReturnContents( $aFile['name'] );
    
        // Out put file 
        $fp = fopen( $file_path.$aFile['name'], 'w' );
        fwrite( $fp, $temp_file );
        fclose( $fp );
    }
}

?>
