package com.eseo.streamnshare.player;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.eseo.streamnshare.model.AudioTrackConfig;
import com.eseo.streamnshare.model.Song;

public abstract class Player implements Runnable {

	protected static final int FIFO_SIZE = 1024 ;


	protected AudioTrack mAudioTrack ;
	protected AudioTrackConfig mConfig = null ;

	protected boolean mError ;  
	protected HandlerPlayer mHandlerPlayer;
	protected StatesPlayer mState  = StatesPlayer.STOPPED ;
	protected boolean mOutputEndOfStream = false ;
	protected boolean mStop = false ;
	protected boolean ready = false ;

	protected Context mContext ;
	
	
	protected Queue<byte[]> chunks = new LinkedList<byte[]>();
	protected Queue<Integer> positions = new LinkedList<Integer>();
	
	protected Song currentSong ;
	

	public void play() {
		if (mState == StatesPlayer.STOPPED) {
			mState = StatesPlayer.PLAYING ;
			mStop = false;
			new Thread(this).start();
		}
		if (mState == StatesPlayer.READY_TO_PLAY) {
			mState = StatesPlayer.PLAYING ;
			synchronized(this){
				notify() ;
			}
		}
	}

	public void stop() {
		mStop = true;
		mState = StatesPlayer.STOPPED;
	}

	public void pause() {
		mState = StatesPlayer.READY_TO_PLAY ;
	}

	public void makePause(){
		while(mState != StatesPlayer.PLAYING) {
			try {
				synchronized(this){
					wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected void configureAudioTrack(){
		int channelConfiguration = mConfig.channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
		int minSize = AudioTrack.getMinBufferSize( mConfig.sampleRate, channelConfiguration, AudioFormat.ENCODING_PCM_16BIT);
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mConfig.sampleRate, channelConfiguration, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
		mAudioTrack.play();
	}

	protected void clear(){

		if(mAudioTrack != null) {
			mAudioTrack.flush();
			mAudioTrack.release();
			mAudioTrack = null;	
		}

		mState = StatesPlayer.STOPPED;
		mStop = false;
		mConfig = null ;
		ready = false ;
		chunks.clear();
		positions.clear();
	}

	public boolean isError() {
		return mError;
	}

	public void setError(boolean error) {
		this.mError = error;
	}

	public AudioTrackConfig getConfig() {
		return mConfig;
	}

	public void setConfig(AudioTrackConfig mConfig) {
		this.mConfig = mConfig;
	}

	public StatesPlayer getState() {
		return mState;
	}

	public long getDuration(){
		return mConfig.duration;
	}

	public Song getCurrentSong() {
		return currentSong;
	}

	public void setCurrentSong(Song currentSong) {
		this.currentSong = currentSong;
	}

	
	
	

}
