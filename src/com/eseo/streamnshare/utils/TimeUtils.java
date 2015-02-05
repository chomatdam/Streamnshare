package com.eseo.streamnshare.utils;

public class TimeUtils {
	
	private static final int SECOND_UNIT = 1000 ;
	private static final int MINUT_UNIT = 60 ;
	
	//Get time from milliseconds
	public static String getTime(long seconds){
		long tMinutes = seconds/ (MINUT_UNIT * SECOND_UNIT) ;
		long tSeconds = (seconds % (MINUT_UNIT*SECOND_UNIT)) / SECOND_UNIT ;
		
		String minutesString = (tMinutes > 9) ?""+ tMinutes : "0"+tMinutes ;
		String secondsString = (tSeconds > 9) ?""+ tSeconds : "0"+tSeconds ;
		
		return minutesString+":"+secondsString;
	}
	
	

}
