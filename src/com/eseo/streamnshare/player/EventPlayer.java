package com.eseo.streamnshare.player;

import com.eseo.streamnshare.model.Song;


public interface EventPlayer {
	
	
	int START = 1 ;
	int PLAY = 2 ;
	int SEEK = 3 ;
	int STOP = 4 ;
	int ERROR = 5 ;
	
	public void onStart(Song song);
	public void onPlay();
	public void onSeek(int percent);
	public void onStop();
	public void onError();
	
}
 