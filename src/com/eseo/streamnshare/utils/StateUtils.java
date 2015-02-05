package com.eseo.streamnshare.utils;

import com.eseo.streamnshare.activities.main.PlayMode;

import android.content.Context;
import android.content.SharedPreferences;

public class StateUtils {

	
	private static final String SHARED_PREFERENCES_NAME = "MyPreferences" ;
	private static final String MODE = "mode" ;
	private static final String STATE = "state" ;
	
	public static void saveMode(Context context, PlayMode mode) {
	    SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME,0);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putString(MODE, mode.toString());
	    editor.commit();
	} 
	 
	public static PlayMode loadMode(Context context) {
	    SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME,0);
	    String myEnumString = sp.getString(MODE, PlayMode.NORMAL.toString());
	    return PlayMode.valueOf(myEnumString);
	} 
	
	public static void saveState(Context context, boolean serviceIsRunning) {
	    SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME,0);
	    SharedPreferences.Editor editor = sp.edit();
	    editor.putBoolean(STATE,serviceIsRunning);
	    editor.commit();
	} 
	 
	public static boolean loadState(Context context) {
	    SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME,0);
	    return sp.getBoolean(STATE,false);
	} 
}
