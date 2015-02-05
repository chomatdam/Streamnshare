package com.eseo.streamnshare.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.activities.main.MainActivity;
import com.eseo.streamnshare.activities.main.PlayMode;
import com.eseo.streamnshare.model.Song;

public class NormalService extends MusicService implements 
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {

	//media player
	private MediaPlayer player;

	@Override
	public void onCreate(){
		super.onCreate();
		initMusicPlayer();
	}
	

	public void initMusicPlayer(){
		player = new MediaPlayer();
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// class set as listener when instance is prepared
		//						 when a song has completed playback
		//						 an error is thrown
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}
	
	@Override
	public void playSong(){
		//play a song
		player.reset();
		//get song
		Song playSong = getCurrentSong();
		//get id
		long currSong = playSong.getId();
		//set uri
		Uri trackUri = ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
		try{
			player.setDataSource(getApplicationContext(), trackUri);
		}catch(Exception e){
			Log.e("MUSIC SERVICE","Error setting data source", e);
		}
		player.prepareAsync();
		
		Intent intent = new Intent(ON_START);
		intent.putExtra(ON_START,playSong);
		mBroadcastManager.sendBroadcast(intent);
		
	}

	@Override
	public void onPrepared(MediaPlayer mp){
		//start playback
		mp.start();
		buildNotification();
		//Broadcast intent to activity: media player prepared !
		Intent onPreparedIntent = new Intent(PLAYER_PREPARED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
	}

	@Override
	public int getPosn(){
		return player.getCurrentPosition();
	}
	@Override
	public int getDur(){
		return player.getDuration();
	}
	@Override
	public boolean isPlaying(){
		return player.isPlaying();
	}
	@Override
	public void pausePlayer(){
		player.pause();
	}
	@Override
	public void seek(int posn){
		player.seekTo(posn);
	}
	@Override
	public void resume(){
		player.start();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		stopForeground(true); //remove notification when service instance is destroyed
		player.stop();
		player.release();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if(player.getCurrentPosition()>0){
			mp.reset();
			playNext();
		}
	}
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}

	
}
