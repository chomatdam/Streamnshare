package com.eseo.streamnshare.screens;

import java.util.ArrayList;
import java.util.Random;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.screens.main.MainActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MusicService extends Service implements 
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {

	//media player
	private MediaPlayer player;
	//song list
	private ArrayList<Song> songs;
	//current position
	private int songPosn;

	//Binding process with Activity
	private final IBinder musicBind = new MusicBinder();

	//back to the app from Notification slider
	private String songTitle="";
	private static final int NOTIFY_ID=1;

	//shuffle playback
	private boolean shuffle=false;
	private Random rand;

	public void onCreate(){
		//create the service
		super.onCreate();
		//init position
		songPosn=0;
		//create player
		player = new MediaPlayer();
		initMusicPlayer();
		rand = new Random();
	}

	public void initMusicPlayer(){
		//set player properties
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// class set as listener when instance is prepared
		//						 when a song has completed playback
		//						 an error is thrown
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}

	public void setList(ArrayList<Song> theSongs){
		songs=theSongs;
	}

	public class MusicBinder extends Binder{
		public MusicService getService(){
			return MusicService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {	//release resources when Service instance is unbound
		return musicBind;
	}

	@Override
	public boolean onUnbind(Intent intent){
		player.stop();
		player.release();
		return false;
	}

	public void playSong(ArrayList<Song> songs){
		this.songs = songs;
		//play a song
		player.reset();
		//get song
		Song playSong = songs.get(songPosn);
		songTitle = playSong.getTitle();
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
	}

	@Override
	public void onPrepared(MediaPlayer mp){
		//start playback
		mp.start();
		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		//PendingIntent send the user from notification bar to Activity
		Notification.Builder builder = new Notification.Builder(this);
		builder.setContentIntent(pendInt)
		.setSmallIcon(R.drawable.ic_action_play)
		.setTicker(songTitle)
		.setOngoing(true)
		.setContentTitle("En cours de lecture")
		.setContentText(songTitle);
		Notification not = builder.getNotification(); // API 16+ (4.1) builder.build();
		startForeground(NOTIFY_ID,not);
		//Broadcast intent to activity: media player prepared !
		Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
		LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
	}

	public void setSong(int songIndex){
		songPosn=songIndex;
	}

	public int getPosn(){
		return player.getCurrentPosition();
	}

	public int getDur(){
		return player.getDuration();
	}

	public boolean isPng(){
		return player.isPlaying();
	}

	public void pausePlayer(){
		player.pause();
	}

	public void seek(int posn){
		player.seekTo(posn);
	}

	public void go(){
		player.start();
	}

	public void playPrev(){
		songPosn--;
		if(songPosn<0) songPosn=songs.size()-1;
		playSong(songs);
	}

	//skip to next
	public void playNext(){
		if(shuffle){
			int newSong = songPosn;
			while(newSong==songPosn){
				newSong=rand.nextInt(songs.size());
			}
			songPosn=newSong;
		}
		else{
			songPosn++;
			if(songPosn>=songs.size()) songPosn=0;
		}
		playSong(songs);
	}

	@Override
	public void onDestroy(){
		stopForeground(true); //remove notification when service instance is destroyed
	}

	public void setShuffle(){
		shuffle=!shuffle;
	}
	
	public boolean getShuffle(){
		return shuffle;
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
