package com.eseo.streamnshare.fragments.fullscreen;

import org.alljoyn.bus.annotation.Position;

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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.services.MusicService;
import com.eseo.streamnshare.services.MusicService.MusicBinder;
import com.eseo.streamnshare.services.SourceService;
import com.eseo.streamnshare.utils.TimeUtils;

public class SourceFragment extends Fragment implements OnClickListener{


	private Button playButton ;
	private Button nextButton ;
	private Button prevButton ;
	private SeekBar seekbar ;
	private TextView currentPosTextView ;
	private TextView durationTextView ;
	

	private MusicService musicService ;


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(false);
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().bindService(new Intent(getActivity(),SourceService.class),musicConnection,0);
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


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_fullscreen_source, container, false);
		seekbar = (SeekBar) view.findViewById(R.id.source_seekbar);
		seekbar.setOnTouchListener(new OnTouchListener(){ 
			@Override 
			public boolean onTouch(View v, MotionEvent event) {
				return true; 
			} 
		}); 

		playButton = (Button) view.findViewById(R.id.source_play_button);
		playButton.setOnClickListener(this);
		nextButton = (Button) view.findViewById(R.id.source_next_button);
		nextButton.setOnClickListener(this);
		prevButton = (Button) view.findViewById(R.id.source_prev_button);
		prevButton.setOnClickListener(this);
		
		currentPosTextView = (TextView) view.findViewById(R.id.source_actualTime_textView);
		durationTextView = (TextView) view.findViewById(R.id.source_duration_textView);
		
		return view ;

	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.source_play_button :
			if(musicService.isPlaying()){
				musicService.pausePlayer();
				playButton.setBackgroundResource(R.drawable.ic_action_play);
			}
			else {
				musicService.resume();
				playButton.setBackgroundResource(R.drawable.ic_action_pause);
			}
			break;

		case R.id.source_next_button :
			musicService.playNext();
			break;

		case R.id.source_prev_button :
			musicService.playPrev();
			break;
		}
	}

	private ServiceConnection musicConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			MusicBinder binder = (MusicBinder)service;
			musicService = binder.getService();

			Song currentSong = musicService.getCurrentSong();
			if(currentSong != null){
				FullScreenInfoFragment fragment = (FullScreenInfoFragment)getFragmentManager().findFragmentById(R.id.fragment_info);
				fragment.setSong(currentSong);
				currentPosTextView.setText("00:00");
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
			}
			else if(i.getAction().equals(MusicService.ON_SEEK)){
				int percent = i.getIntExtra(MusicService.ON_SEEK,seekbar.getProgress());
				seekbar.setProgress(percent);
				long position = (long)(((double)percent/(double)1000) * musicService.getCurrentSong().getDuration()) ;
				currentPosTextView.setText(TimeUtils.getTime(position));
			}
			
			else if(i.getAction().equals(MusicService.ON_STOP)){
				currentPosTextView.setText("00:00");
				seekbar.setProgress(0);
			}
			
			
		}
	};


}
