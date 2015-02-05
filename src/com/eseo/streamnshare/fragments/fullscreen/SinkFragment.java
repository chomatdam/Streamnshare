package com.eseo.streamnshare.fragments.fullscreen;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.services.MusicService;
import com.eseo.streamnshare.services.MusicService.MusicBinder;
import com.eseo.streamnshare.services.SinkService;
import com.eseo.streamnshare.utils.TimeUtils;

public class SinkFragment extends Fragment{
	
	private MusicService musicService ;
	
	private TextView currentPosTextView ;
	private TextView durationTextView ;
	private SeekBar seekbar ;
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_fullscreen_sink, container, false);
		
		seekbar = (SeekBar) view.findViewById(R.id.sink_seekbar);
		seekbar.setOnTouchListener(new OnTouchListener(){ 
			@Override 
			public boolean onTouch(View v, MotionEvent event) {
				return true; 
			} 
		}); 
		
		currentPosTextView = (TextView) view.findViewById(R.id.sink_actualTime_textView);
		durationTextView = (TextView) view.findViewById(R.id.sink_duration_textView);
		
		return view ;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getActivity().bindService(new Intent(getActivity(),SinkService.class),musicConnection,0);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadCastReceiver,new IntentFilter(MusicService.ON_SEEK));
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadCastReceiver,new IntentFilter(MusicService.ON_STOP));
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadCastReceiver,new IntentFilter(MusicService.ON_START));
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unbindService(musicConnection);
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadCastReceiver);
		
	}
	
	
	private ServiceConnection musicConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			MusicBinder binder = (MusicBinder)service;
			musicService = binder.getService();
			currentPosTextView.setText("00:00");
			Song currentSong = musicService.getCurrentSong();
			if(currentSong != null){
				FullScreenInfoFragment fragment = (FullScreenInfoFragment)getFragmentManager().findFragmentById(R.id.fragment_info);
				fragment.setSong(currentSong);
				durationTextView.setText(TimeUtils.getTime(currentSong.getDuration()));
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name){

		}
	};
	
	private BroadcastReceiver mBroadCastReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context c, Intent i){
			if(i.getAction().equals(MusicService.ON_START)){
				Song currentSong = (Song)i.getParcelableExtra(MusicService.ON_START);
				FullScreenInfoFragment fragment = (FullScreenInfoFragment)getFragmentManager().findFragmentById(R.id.fragment_info);
				fragment.setSong(currentSong);
				currentPosTextView.setText("00:00");
				durationTextView.setText(TimeUtils.getTime(currentSong.getDuration()));
				musicService.playSong();
			}
			else if(i.getAction().equals(MusicService.ON_SEEK)){
				int percent = i.getIntExtra(MusicService.ON_SEEK,seekbar.getProgress());
				seekbar.setProgress(percent);
				long position = (long)(((double)percent/(double)1000) * musicService.getCurrentSong().getDuration()) ;
				currentPosTextView.setText(TimeUtils.getTime(position));
				
			}
			
			else if(i.getAction().equals(MusicService.ON_STOP)){
				seekbar.setProgress(0);
				currentPosTextView.setText("00:00");
			}
			
			
		}
	};

	
	

}
