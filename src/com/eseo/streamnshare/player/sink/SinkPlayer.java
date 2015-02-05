package com.eseo.streamnshare.player.sink;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.alljoyn.bus.annotation.BusSignalHandler;

import android.content.Context;
import android.os.Message;
import android.os.Process;

import com.eseo.streamnshare.model.AudioMetadata;
import com.eseo.streamnshare.model.AudioTrackConfig;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.player.EventPlayer;
import com.eseo.streamnshare.player.HandlerPlayer;
import com.eseo.streamnshare.player.Player;
import com.eseo.streamnshare.player.StatesPlayer;

public class SinkPlayer extends Player{

	private SinkActionner mSinkActionner ;

	private boolean isPlaying = true  ;

	

	public SinkPlayer(Context context) 
	{
		mHandlerPlayer = new HandlerPlayer(this);
		mContext = context ;
		mSinkActionner = new SinkActionner(mContext, this);
	}

	public SinkPlayer(Context context, EventPlayer events) {
		mHandlerPlayer = new HandlerPlayer(this,events);
		mContext = context ;
		mSinkActionner = new SinkActionner(mContext, this);
		
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

		while(isPlaying){
			pause();
			ready = false ;

			while (!mStop) {
				makePause();
				if(!chunks.isEmpty()){
					byte[] chunk = chunks.remove();
					int position = positions.remove();
					Message msg = mHandlerPlayer.obtainMessage(EventPlayer.SEEK,position);
					mHandlerPlayer.sendMessage(msg);
					mAudioTrack.write(chunk,0,chunk.length);
				}
			}
			clear();
			mHandlerPlayer.sendEmptyMessage(EventPlayer.STOP);
		}
	}

	@BusSignalHandler(iface="com.eseo.streamnshare.player.source",signal="signalAudioTrackConfig")
	public void receivedAudioConfig(AudioTrackConfig config) {
		if(mConfig == null){
			mConfig = config ;
			configureAudioTrack();
			Message startMessage = mHandlerPlayer.obtainMessage(EventPlayer.START,currentSong);
			mHandlerPlayer.sendMessage(startMessage);
		}
	}

	@BusSignalHandler(iface="com.eseo.streamnshare.player.source",signal="signalAudioData")
	public void receivedAudioData(byte[] data,int position) {
		if(mConfig != null){
			chunks.add(data) ;
			positions.add(position);
			if(!ready && chunks.size() == FIFO_SIZE/4){
				mSinkActionner.sendSignalFifo();
				ready = true ;
				play();
			}
		}
	}

	@BusSignalHandler(iface="com.eseo.streamnshare.player.source",signal="signalMetadata")
	public void receivedMetadata(AudioMetadata metadata) {
		currentSong = new Song();
		currentSong.setDuration(metadata.duration);
		currentSong.setAlbum_name(metadata.albumName);
		currentSong.setArtist(metadata.artistName);
		currentSong.setTitle(metadata.songName);
	}

	@BusSignalHandler(iface="com.eseo.streamnshare.player.source",signal="signalStatePlayer")
	public void receivedStatePlayer(StatesPlayer state) {
		switch(state){
		case PLAYING:
			play();
			break;
		case STOPPED:
			stop();
			break;
		case READY_TO_PLAY:
			pause();
			break;
		}
	}


	public SinkActionner getSinkActionner() {
		return mSinkActionner;
	}

	public void setSinkActionner(SinkActionner mSinkActionner) {
		this.mSinkActionner = mSinkActionner;
	}

	public void stopPlayer(){
		isPlaying = false ;
	}
	public void startPlayer(){
		isPlaying = true ;
	}

	
	
	
}
