package com.eseo.streamnshare.screens.fullscreen;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.fragments.fullscreen.PlayingFragment;
import com.eseo.streamnshare.managers.MusicManager;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.screens.MusicController;
import com.eseo.streamnshare.screens.MusicService;
import com.eseo.streamnshare.screens.MusicService.MusicBinder;

public class FullScreenActivity extends FragmentActivity implements MediaPlayerControl{

	//Music
	private ArrayList<Song> songList;

	//service
	private MusicService musicSrv;
	private Intent playIntent;
	private boolean musicBound=false;

	//controller
	private MusicController controller;

	//leaving app or pausing playback
	private boolean paused=false;
	private boolean playbackPaused=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		final ActionBar actionBar = getActionBar();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC); 
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		/* Custom action bar */
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));

		songList = MusicManager.getInstance(this).getSongList();

		/* Fragments */
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fullscreen_fragment_placeholder, new PlayingFragment());
		ft.commit();
		
		/* Set music */
		setController();
	}

	/* Connection */
	private ServiceConnection musicConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			MusicBinder binder = (MusicBinder)service;
			//get service
			musicSrv = binder.getService();
			//pass list
			musicSrv.setList(songList);
			musicBound = true;
			//TODO: SALE > A deplacer ?
			Bundle b = getIntent().getExtras();
			int positionSong = b.getInt("positionSong");
			songList = b.getParcelableArrayList("songListKey");
			songPicked(positionSong);
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

	@Override
	protected void onDestroy(){
		stopService(playIntent);
		musicSrv=null;
		super.onDestroy();
	}


	/*
	 * Launch music from main activity
	 */

	void songPicked(int position){
		musicSrv.setSong(position);
		musicSrv.playSong(songList);
		if(playbackPaused){
			setController();
			playbackPaused=false;
		}
		controller.show(0);
	}





	/*
	 * Menu items
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.fullscreen, menu);
		return true/*super.onCreateOptionsMenu(menu)*/;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		//menu item selected
		switch(item.getItemId()){
		case R.id.action_shuffle:
			//shuffle
			musicSrv.setShuffle();
			updateShuffleMenuItem(item);
			return true;
		case R.id.action_end:
			stopService(playIntent);
			musicSrv=null;
			System.exit(0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	public void updateShuffleMenuItem(MenuItem shuffleMenuItem) {
		if(musicSrv!=null){
			if(musicSrv.getShuffle()){
				shuffleMenuItem.setTitle(R.string.action_noshuffle);
				shuffleMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_action_shuffle_selected));
			}
			else{
				shuffleMenuItem.setTitle(R.string.action_shuffle);
				shuffleMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_action_shuffle));
			}
		}
	}







	/*
	 * MediaPlayerControl implementation
	 */

	private void setController(){
		//set the controller up
		if (controller == null) controller = new MusicController(this,false);
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
		controller.setAnchorView(findViewById(R.id.fullscreen_fragment_placeholder));
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
		return false;
	}


	@Override
	public boolean canSeekForward() {
		return false;
	}


	@Override
	public int getAudioSessionId() {
		return 0;
	}


	@Override
	public int getBufferPercentage() {
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
		LocalBroadcastManager.getInstance(this).registerReceiver(onPrepareReceiver,new IntentFilter("MEDIA_PLAYER_PREPARED"));
		if(paused){
			setController();
			paused=false;
		}
	}

	//Broadcast receiver waiting for music played prepared
	private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context c, Intent i){
			// show controller when it's ready
			controller.show(0);
		}
	};

	@Override
	protected void onStop(){
		super.onStop();
		controller.hide();
	}

}
