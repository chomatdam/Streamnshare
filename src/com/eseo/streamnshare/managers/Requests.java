package com.eseo.streamnshare.managers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class Requests{

	private static final String URI = "http://api.musixmatch.com/ws/1.1/";
	private static final String KEY = "dbd873db3fcbef4706df0c8f9f0342eb";

	private static Requests instance = null;

	public Requests(){}

	public static Requests getInstance(){
		if(instance==null){
			instance = new Requests();
		}
		return instance;
	}

	public String trackRequest(String artist, String title){
		String request = URI+"matcher.track.get?apikey="+KEY;

		try {
			if(!artist.equals("<unknown>")){
				request+="&q_artist="+URLEncoder.encode(artist, "UTF-8");
			}
			if(!title.equals("<unknown>")){
				request += "&q_track="+URLEncoder.encode(title, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return request;
	}


}
