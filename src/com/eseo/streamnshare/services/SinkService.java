package com.eseo.streamnshare.services;

import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.player.EventPlayer;
import com.eseo.streamnshare.player.StatesPlayer;
import com.eseo.streamnshare.player.sink.SinkActionner;
import com.eseo.streamnshare.player.sink.SinkPlayer;


public class SinkService extends StreamingService  {

	static {
		System.loadLibrary("alljoyn_java");
	}
	
	private SinkPlayer mSinkPlayer ;
	private SinkActionner mSinkActionner ;
	
	
	@Override
	public void onCreate(){
		super.onCreate();
		mSinkPlayer = new SinkPlayer(this,events);
		mSinkActionner = mSinkPlayer.getSinkActionner();
		
		mSinkPlayer.play();
		
		mSinkActionner.startBusHandler();
		mSinkActionner.connect();
		mSinkActionner.startDiscovery();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mSinkPlayer.stop();
		mSinkPlayer.stopPlayer();
		mSinkActionner.stopDiscovery();
		mSinkActionner.quitSession();
		mSinkActionner.disconnect();
		mSinkActionner.stopBusHandler();
	}
	
	@Override
	public Song getCurrentSong() {
		return mSinkPlayer.getCurrentSong();
	}
	
	@Override
	public void playSong() {
		buildNotification();
	}

	@Override
	public int getPosn() {
		return 0 ;
	}

	@Override
	public int getDur() {
		return 0 ;
	}

	@Override
	public boolean isPlaying() {
		return mSinkPlayer.getState() == StatesPlayer.PLAYING ? true : false ;
	}

	@Override
	public void pausePlayer() {
	}

	@Override
	public void seek(int posn) {
	}

	@Override
	public void resume() {
	}

	
	
	

}
