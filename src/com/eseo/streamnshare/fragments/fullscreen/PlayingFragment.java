package com.eseo.streamnshare.fragments.fullscreen;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController.MediaPlayerControl;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.activities.MusicController;
import com.eseo.streamnshare.activities.MusicService;
import com.eseo.streamnshare.activities.MusicService.MusicBinder;
import com.eseo.streamnshare.model.Song;


public class PlayingFragment extends Fragment implements MediaPlayerControl{
	
	private ArrayList<Song> songList;
	
	//service
	private MusicService musicSrv;
	private Intent playIntent;
	private boolean musicBound=false;

	//controller
	private MusicController controller;
	private int positionSong;

	//leaving app or pausing playback
	private boolean paused=false;
	private boolean playbackPaused=false;
	
	/*
	 * Fragment part
	 */

	public PlayingFragment(){
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setController();
		songList = this.getArguments().getParcelableArrayList("songListKey");
		positionSong = this.getArguments().getInt("positionSong");
        setHasOptionsMenu(true);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_fullscreen_playing, container, false);
	}
	

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
	    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	        super.onCreateOptionsMenu(menu, inflater);
	        inflater.inflate(R.menu.fullscreen, menu);
	    }
	  
	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	       // handle item selection
	       switch (item.getItemId()) {
			case R.id.action_shuffle:
				//shuffle
				musicSrv.setShuffle();
				updateShuffleMenuItem(item);
				return true;
			case R.id.action_end:
				getActivity().stopService(playIntent);
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
	 * ServiceConnection + onStart/Destroy fragment
	 */
	
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
			//Launch song when the service is ready
			songPicked(positionSong);
		}

		@Override
		public void onServiceDisconnected(ComponentName name){
			musicBound = false;
		}
	};

	@Override
	public void onStart(){
		super.onStart();
		if(playIntent==null){
			playIntent = new Intent(getActivity(), MusicService.class); 
			getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			getActivity().startService(playIntent);
		}

	}
	
	@Override
	public void onDestroy(){
		getActivity().stopService(playIntent);
		musicSrv=null;
		super.onDestroy();
	}
	
	
	
	
	/*
	 * MediaPlayerControl implementation
	 */

	private void setController(){
		//set the controller up
		if (controller == null) controller = new MusicController(getActivity(), false);
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
		controller.setAnchorView(getActivity().findViewById(R.id.fullscreen_fragment_placeholder));
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
	public void onPause(){
		super.onPause();
		paused=true;
	}

	@Override
	public void onResume(){
		super.onResume();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onPrepareReceiver,new IntentFilter("MEDIA_PLAYER_PREPARED"));
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
	public void onStop(){
		super.onStop();
		controller.hide();
	}

}