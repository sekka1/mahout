package io.algorithms.util;

import java.io.File;
//Helper class that lets one set the Operating system up
public class OS {
	
	private static OS _instance;
	private boolean _bWin;
	
	private OS()
	{
		String os = System.getProperty("os.name");
        if (os!=null && os.toLowerCase().contains("win") || _bWin)
        	_bWin =  true;
        else
        	_bWin = false;
	};
	
	public static OS getInstance()
	{
		if (_instance==null)
			_instance = new OS();
		
		return _instance;
	}
	
	
	public boolean isWin()
	{
		return _bWin;
	}
	
	public void setIsWin( boolean b )
	{
		_bWin = b;
	}
	
	public String fixSlashesForWin( String s_in )
	{
		String ans = s_in;
		ans = ans.replace("/", "\\");
		return ans;
	}
}
