package com.eseo.streamnshare.services;

import java.util.ArrayList;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.activities.main.MainActivity;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.utils.StateUtils;

public abstract class  MusicService extends Service  {

	private static final int NOTIFY_ID=1;

	public static final String PLAYER_PREPARED = "PLAYER_PREPARED" ;
	public static final String ON_START = "ON_START" ;
	public static final String ON_SEEK = "ON_SEEK" ;
	public static final String ON_STOP = "ON_STOP" ;

	//song list
	protected ArrayList<Song> songs;
	//current position
	protected int songPosn;

	//shuffle playback
	protected boolean shuffle=false;
	protected Random rand;

	protected final IBinder musicBinder = new MusicBinder();

	protected  LocalBroadcastManager mBroadcastManager ;


	public class MusicBinder extends Binder {
		public MusicService getService(){
			return MusicService.this ;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {	
		return musicBinder;
	}


	@Override
	public void onCreate(){
		//create the service
		super.onCreate();
		//init position
		songPosn=0;
		//create player
		rand = new Random();
		mBroadcastManager = LocalBroadcastManager.getInstance(this);
		StateUtils.saveState(this,true);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		StateUtils.saveState(this,false);
	}


	public void setList(ArrayList<Song> theSongs){
		songs=theSongs;
	}

	public abstract void playSong();

	public abstract int getPosn();

	public abstract int getDur();

	public abstract boolean isPlaying();

	public abstract void pausePlayer();

	public abstract void seek(int posn);

	public abstract void resume();


	public void playPrev(){
		songPosn--;
		if(songPosn<0) songPosn=songs.size()-1;
		playSong();
	}


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
		playSong();
	}

	public void setShuffle(){
		shuffle=!shuffle;
	}

	public boolean getShuffle(){
		return shuffle;
	}

	public Song getCurrentSong(){
		if(songs != null && !songs.isEmpty())
			return songs.get(songPosn);
		else
			return null ;
	}

	public void setCurrentSong(int songIndex){
		songPosn=songIndex;
	}

	public void buildNotification(){

		Song playSong = getCurrentSong();
		if(playSong != null){
			Intent notIntent = new Intent(this, MainActivity.class);
			notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			//PendingIntent send the user from notification bar to Activity
			Notification.Builder builder = new Notification.Builder(this);
			builder.setContentIntent(pendInt)
			.setSmallIcon(R.drawable.ic_action_play)
			.setLargeIcon(playSong.getBitmap(getApplicationContext()))
			.setTicker(playSong.getTitle())
			.setAutoCancel(true)
			.setContentTitle(playSong.getTitle())
			.setContentText(playSong.getArtist());
			Notification not = builder.build(); // API 16+ (4.1) builder.build();
			startForeground(NOTIFY_ID,not);
		}
	}



}
