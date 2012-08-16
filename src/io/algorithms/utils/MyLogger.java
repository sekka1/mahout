package io.algorithms.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;

// TODO: Auto-generated Javadoc
/**
 * The Class MyLogger.
 */
public class MyLogger {
	
	private MyLogger(){}
	
	private static MyLogger _instance;
	private String _filename;
	public static MyLogger getInstance()
	{
		if (_instance == null)
			_instance = new MyLogger();
		return _instance;
	}
	/**
	 * Sets the log file name.
	 *
	 * @param in_filename the new log file name
	 */
	public void setLogFileName( String in_filename )
	{
		_filename = in_filename;
	}
	
	//it clears it but it also returns what it cleared first!
	public String clearLog()
	{
		String ans = null;
		try {
		File myFile = new File(_filename);
		Scanner scan;
		scan = new Scanner(myFile);
		scan.useDelimiter("\\Z");  
	    ans = scan.next();
	    FileWriter fstream;
		
		fstream = new FileWriter(_filename, false); //write!
		
		BufferedWriter out = new BufferedWriter(fstream);
		out.write("");
		out.close();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return ans;
	    
	}
	public String get_filename() {
		return _filename;
	}
	public void log( int message )
	{
		log(String.valueOf(message));
	}
	public void log( String message )
	{
		try {
			FileWriter fstream;
		
			fstream = new FileWriter(_filename, true); //append!
			
			BufferedWriter out = new BufferedWriter(fstream);
			out.write( message + "\n");
			out.close();
			
		} catch (IOException e) {
			System.out.println("rob error: Logger Error : " + e.getMessage());
		}
		
	}

/**
 * Gets the stack trace.
 *
 * @param throwable the throwable
 * @return the stack trace
 */
public static String getStackTrace(Throwable throwable) {
	            StringWriter sw = new StringWriter();
	            PrintWriter pw = new PrintWriter(sw, true);
	            throwable.printStackTrace(pw);
	            return sw.getBuffer().toString();
	        }
}
