package com.eseo.streamnshare.fragments.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.activities.fullscreen.FullScreenActivity;
import com.eseo.streamnshare.activities.main.MainActivity;
import com.eseo.streamnshare.managers.MusicManager;
import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.services.MusicService;

public class MainControllerFragment extends Fragment{

	private boolean playing ;

	private ImageView coverArtImageView ;
	private TextView titleTextView ;
	private TextView artistTextView ;
	private Button controllerButton ;


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onPrepareReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onPrepareReceiver,new IntentFilter(MusicService.ON_START));

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		View rootView = inflater.inflate(R.layout.fragment_main_controller, container, false);

		coverArtImageView = (ImageView) rootView.findViewById(R.id.controller_imageView);
		titleTextView = (TextView) rootView.findViewById(R.id.controller_title_textView);
		artistTextView = (TextView) rootView.findViewById(R.id.controller_artist_textView);
		controllerButton = (Button) rootView.findViewById(R.id.controller_button);
		controllerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				if(playing){
					activity.getMusicService().pausePlayer();
				}
				else {
					activity.getMusicService().resume();
				}
				setPlaying(!playing);
			}
		});

		rootView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity activity = (MainActivity) getActivity();
				MusicService musicService = activity.getMusicService();

				if(musicService.isPlaying()){
					Intent intent = new Intent(activity, FullScreenActivity.class);
					Bundle b = new Bundle();
					b.putSerializable("mode",activity.getPlayMode());
					intent.putExtras(b);
					startActivity(intent);
				}
				

		}
	});
		return rootView;
}

public void setSong(Song song){
	
	Bitmap oldBitmap = coverArtImageView.getDrawingCache();
	if(oldBitmap != null){
		oldBitmap.recycle();
	}
	
	Bitmap bitmap = song.getBitmap(getActivity());
	if(bitmap != null){
		coverArtImageView.setImageBitmap(bitmap);
	}
	else {
		MusicManager.getInstance(getActivity()).getCovertArt(song,coverArtImageView);
	}
	titleTextView.setText(song.getTitle());
	artistTextView.setText(song.getArtist());

}


public boolean isPlaying() {
	return playing;
}

public void setPlaying(boolean playing) {
	this.playing = playing;
	if(this.playing){
		controllerButton.setBackgroundResource(R.drawable.ic_action_pause);
	}
	else {
		controllerButton.setBackgroundResource(R.drawable.ic_action_play);
	}
}

private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver(){
	@Override
	public void onReceive(Context c, Intent i){
		if(i.getAction().equals(MusicService.ON_START)){
			Song currentSong = (Song)i.getParcelableExtra(MusicService.ON_START);
			setSong(currentSong);
			if(!playing){
				setPlaying(true);
			}
		}
	}
};




}
