package com.eseo.streamnshare.services;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.eseo.streamnshare.player.StatesPlayer;
import com.eseo.streamnshare.player.source.SourceActionner;
import com.eseo.streamnshare.player.source.SourcePlayer;


public class SourceService extends StreamingService  {

	static {
		System.loadLibrary("alljoyn_java");
	}
	
	private SourcePlayer mSourcePlayer ;
	private SourceActionner mSourceActionner ;
	
	@Override
	public void onCreate(){
		super.onCreate();
		mSourcePlayer = new SourcePlayer(this,events);
		mSourceActionner = mSourcePlayer.getSourceActionner() ;
		
		mSourceActionner.startBusHandler();
		mSourceActionner.connect();
		mSourceActionner.startAdvertisingName();
		mSourceActionner.bindSession();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mSourcePlayer.stop();
		mSourceActionner.unbindSession();
		mSourceActionner.stopAdvertisingName();
		mSourceActionner.disconnect();
		mSourceActionner.stopBusHandler();
	}

	@Override
	public void playSong() {
		mSourcePlayer.stop();
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				while(!mSourcePlayer.isAvailable()){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
				}
				mSourcePlayer.setSource(getCurrentSong());
				
				mSourcePlayer.play();
			} 
		
		}).start();	
		
		buildNotification();
		
		Intent intent = new Intent(ON_START);
		intent.putExtra(ON_START,getCurrentSong());
		mBroadcastManager.sendBroadcast(intent);
	}

	@Override
	public int getPosn() {
		return (int)mSourcePlayer.getPosition();
	}

	@Override
	public int getDur() {
		return (int)mSourcePlayer.getDuration();
	}

	@Override
	public boolean isPlaying() {
		return mSourcePlayer.getState() == StatesPlayer.PLAYING ? true : false ;
	}

	@Override
	public void pausePlayer() {
		mSourcePlayer.pause();
	}

	@Override
	public void seek(int posn) {
		mSourcePlayer.seek(posn);
	}

	@Override
	public void resume() {
		mSourcePlayer.play();
	}

	
	
	

}
