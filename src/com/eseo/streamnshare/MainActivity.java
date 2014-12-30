package com.eseo.streamnshare;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.eseo.streamnshare.MusicService.MusicBinder;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends Activity implements MediaPlayerControl{

	//view
	private ArrayList<Song> songList;
	private ListView songView;

	//service
	private MusicService musicSrv;
	private Intent playIntent;
	private boolean musicBound=false;

	//controller
	private MusicController controller;
	
	//leaving app or pausing playback
	private boolean paused=false, playbackPaused=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		songView = (ListView)findViewById(R.id.song_list); //On récup la ListView 'song_list' du main layout
		songList = new ArrayList<Song>();
		getSongList();
		Collections.sort(songList, new Comparator<Song>(){ //Java 7, easier way with Java 8
			public int compare(Song a, Song b){
				return a.getTitle().compareTo(b.getTitle());
			}
		});
		SongAdapter songAdt = new SongAdapter(this, songList);
		songView.setAdapter(songAdt);
		setController();
	}

	//connect to the service -- instance ServiceConnection + 2 methods Connected/Discnt implemented for it with musicBound flag
	private ServiceConnection musicConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			MusicBinder binder = (MusicBinder)service;
			//get service
			musicSrv = binder.getService();
			//pass list
			musicSrv.setList(songList);
			musicBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name){
			musicBound = false;
		}
	};


	@Override
	protected void onStart(){
		super.onStart();
		//override onStart to start Service instance when Activity instance start
		if(playIntent==null){
			playIntent = new Intent(this, MusicService.class); 
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}


	public void getSongList(){
		//Resolver: provide access to Content Provider (We ask to Provider a result(data) as a client)
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI; //define URI
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null); // result

		if(musicCursor!=null && musicCursor.moveToFirst()){
			//récup colonnes
			int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			// remplissage liste
			do{
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist));
			}while(musicCursor.moveToNext());
		}
	}

	public void songPicked(View view){
		musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
		musicSrv.playSong();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0); // never hide music control bar
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		//menu item selected
		switch(item.getItemId()){
		case R.id.action_shuffle:
			//shuffle
			musicSrv.setShuffle();
			break;
		case R.id.action_end:
			stopService(playIntent);
			musicSrv=null;
			System.exit(0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy(){
		stopService(playIntent);
		musicSrv=null;
		super.onDestroy();
	}

	private void setController(){
		//set the controller up
		controller = new MusicController(this);
		controller.setPrevNextListeners(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				playNext();
			}
		}, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				playPrev();
			}
		});
		controller.setMediaPlayer(this);
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
	}

	//play next
	private void playNext(){
		musicSrv.playNext();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}

	//play previous
	private void playPrev(){
		musicSrv.playPrev();
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}


	@Override
	public boolean canPause() {
		return true;
	}


	@Override
	public boolean canSeekBackward() {
		return true;
	}


	@Override
	public boolean canSeekForward() {
		return true;
	}


	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getCurrentPosition() {
		if(musicSrv!=null && musicBound &&musicSrv.isPng()) //si service!=null, Service bound to Activity, isPlaying
			return musicSrv.getPosn();
		else return 0;
	}


	@Override
	public int getDuration() {
		if(musicSrv!=null && musicBound && musicSrv.isPng())
			return musicSrv.getDur();
		else return 0;
	}


	@Override
	public boolean isPlaying() {
		if(musicSrv!=null && musicBound)
			return  musicSrv.isPng();
		else return false;
	}


	@Override
	public void pause() {
		playbackPaused=true;
		musicSrv.pausePlayer();
	}


	@Override
	public void seekTo(int pos) {
		musicSrv.seek(pos);
	}


	@Override
	public void start() {
		musicSrv.go();
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		paused=true;
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(paused){
			setController();
			paused=false;
		}
	}
	
	@Override
	protected void onStop(){
		controller.hide();
		super.onStop();
	}
}
