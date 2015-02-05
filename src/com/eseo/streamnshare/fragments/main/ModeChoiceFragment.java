package com.eseo.streamnshare.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.activities.main.MainActivity;
import com.eseo.streamnshare.activities.main.OnModeChangeListener;
import com.eseo.streamnshare.activities.main.PlayMode;

public class ModeChoiceFragment extends Fragment implements View.OnClickListener{

	private OnModeChangeListener modeChangeListener ;

	private ImageButton streamFrom ;
	private ImageButton streamTo ;
	private ImageButton play ;

	private PlayMode lastMode ;


	public ModeChoiceFragment(){
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		try {
			modeChangeListener = (OnModeChangeListener) activity;

		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnItemSelectedListener");
		}
	}
	 @Override
	public void onResume() {
		super.onResume();
		lastMode = ((MainActivity)getActivity()).getPlayMode() ;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		View rootView = inflater.inflate(R.layout.fragment_mode_choice, container, false);
		streamFrom = (ImageButton)rootView.findViewById(R.id.stream_from);
		streamFrom.setOnClickListener(this);
		play = (ImageButton)rootView.findViewById(R.id.play_button);
		play.setOnClickListener(this);
		streamTo = (ImageButton)rootView.findViewById(R.id.stream_to);
		streamTo.setOnClickListener(this);

		return rootView;
	}


	@Override
	public void onClick(View v) {
		MainActivity activity = (MainActivity) getActivity();

		switch(v.getId()){
		case R.id.stream_from:
			activity.setPlayMode(PlayMode.SINK);
			break;
		case R.id.play_button:
			activity.setPlayMode(PlayMode.NORMAL);
			break;
		case R.id.stream_to:
			activity.setPlayMode(PlayMode.SOURCE);
			break;
		}

		update();
	}

	public void update(){
		PlayMode actualMode = ((MainActivity)getActivity()).getPlayMode();
		if(actualMode != lastMode){
			updateView(actualMode);
			modeChangeListener.onModeChange(lastMode);
			lastMode = actualMode ;
		}
	}
	
	public void updateView(PlayMode actualMode){
		switch(actualMode){
		case NORMAL:
			streamFrom.setImageResource(R.drawable.ic_action_headphones);
			play.setImageResource(R.drawable.ic_action_play_selected);
			streamTo.setImageResource(R.drawable.ic_action_network_wifi);
			break ;
		case SINK :
			streamFrom.setImageResource(R.drawable.ic_action_headphones_selected);
			play.setImageResource(R.drawable.ic_action_play);
			streamTo.setImageResource(R.drawable.ic_action_network_wifi);
			break;
		case SOURCE:
			streamFrom.setImageResource(R.drawable.ic_action_headphones);
			play.setImageResource(R.drawable.ic_action_play);
			streamTo.setImageResource(R.drawable.ic_action_network_wifi_selected);
			break;
		}
	}
	



}
