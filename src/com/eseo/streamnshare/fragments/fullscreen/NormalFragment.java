package com.eseo.streamnshare.fragments.fullscreen;

import java.util.Currency;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.services.MusicService;
import com.eseo.streamnshare.services.MusicService.MusicBinder;
import com.eseo.streamnshare.services.NormalService;


public class NormalFragment extends Fragment implements MediaPlayerControl{

	//service
	private MusicService musicService;
	private boolean musicBound=false;

	//controller
	private MusicController controller;
	
	/*
	 * Fragment part
	 */
	public NormalFragment(){
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_fullscreen_normal, container, false);
	}

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
			musicService.setShuffle();
			updateShuffleMenuItem(item);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void updateShuffleMenuItem(MenuItem shuffleMenuItem) {
		if(musicService!=null){
			if(musicService.getShuffle()){
				shuffleMenuItem.setTitle(R.string.action_noshuffle);
				shuffleMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_action_shuffle_selected));
			}
			else{
				shuffleMenuItem.setTitle(R.string.action_shuffle);
				shuffleMenuItem.setIcon(getResources().getDrawable(R.drawable.ic_action_shuffle));
			}
		}
	}
	/* Connection */
	private ServiceConnection musicConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			MusicBinder binder = (MusicBinder)service;
			musicService = binder.getService();
			musicBound = true ;
			FullScreenInfoFragment fragment = (FullScreenInfoFragment)getFragmentManager().findFragmentById(R.id.fragment_info);
			fragment.setSong(musicService.getCurrentSong());
			controller.show(0);
		}

		@Override
		public void onServiceDisconnected(ComponentName name){

		}
	};

	@Override
	public void onPause(){
		super.onPause();
		musicBound = false;
		getActivity().unbindService(musicConnection);
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadCastReceiver);
		musicService=null;
	}


	@Override
	public void onResume(){
		super.onResume();
		setController();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				getActivity().bindService(new Intent(getActivity(), NormalService.class), musicConnection,0);
				LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadCastReceiver,new IntentFilter(MusicService.PLAYER_PREPARED));
				LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadCastReceiver,new IntentFilter(MusicService.ON_START));
			}
		},100);
	}

	/*
	 * MediaPlayerControl implementation
	 */

	private void setController(){
		if (controller == null) 
		{
			controller = new MusicController(getActivity(), false);
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
			controller.setAnchorView(getActivity().findViewById(R.id.fullscreen_fragment_placeholder));
			controller.setMediaPlayer(this);
			controller.setEnabled(true);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		controller.hide();
	}

	//play next
	private void playNext(){
		musicService.playNext();
	}

	//play previous
	private void playPrev(){
		musicService.playPrev();
	}


	public void showController(){
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
		if(musicService!=null && musicBound )
			return musicService.getPosn();
		else return 0;
	}


	@Override
	public int getDuration() {
		if(musicService!=null && musicBound)
			return musicService.getDur();
		else return 0;
	}


	@Override
	public boolean isPlaying() {
		if(musicService!=null && musicBound)
			return  musicService.isPlaying();
		else return false;
	}

	@Override
	public void pause() {
		musicService.pausePlayer();
	}

	@Override
	public void seekTo(int pos) {
		musicService.seek(pos);
	}

	@Override
	public void start() {
		musicService.resume();
	}

	//Broadcast receiver waiting for music played prepared or refresh cover art
	private BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context c, Intent i){
			if(i.getAction().equals(MusicService.PLAYER_PREPARED)){
				controller.show(0);
			}
			else if(i.getAction().equals(MusicService.ON_START)){
				Song currentSong = (Song)i.getParcelableExtra(MusicService.ON_START);
				FullScreenInfoFragment fragment = (FullScreenInfoFragment)getFragmentManager().findFragmentById(R.id.fragment_info);
				fragment.setSong(musicService.getCurrentSong());
			}
		}
	};

	
	
	
	private class MusicController extends MediaController{

		public MusicController(Context c){
			super(c);
		}

		public MusicController(Context c, Boolean useFastForward){
			super(c,useFastForward);
		}
		@Override
		public void hide() {
		}
		
		@Override
		public boolean dispatchKeyEvent(KeyEvent event)
		{
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
				((Activity) getContext()).finish();

			return super.dispatchKeyEvent(event);
		}
	}





}