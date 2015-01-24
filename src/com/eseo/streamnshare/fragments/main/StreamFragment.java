package com.eseo.streamnshare.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.fragments.OnItemSelectedFragmentListener;

public class StreamFragment extends Fragment implements View.OnClickListener{

	private OnItemSelectedFragmentListener listener;

	public StreamFragment(){
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
			listener = (OnItemSelectedFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnItemSelectedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		View rootView = inflater.inflate(R.layout.fragment_stream, container, false);
		ImageButton streamFrom = (ImageButton)rootView.findViewById(R.id.stream_from);
		streamFrom.setOnClickListener(this);
		ImageButton play = (ImageButton)rootView.findViewById(R.id.play_button);
		play.setOnClickListener(this);
		ImageButton streamTo = (ImageButton)rootView.findViewById(R.id.stream_to);
		streamTo.setOnClickListener(this);

		return rootView;
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.stream_from:
			/*
			// TODO: indiquer le nom de l'activité à lancer
			Intent intent = new Intent(getActivity(), GNAGNARD_ACTIVITY.class);
			startActivity(intent);
			*/
			break;
		case R.id.play_button:
			listener.onSongItemSelected(0);
			break;
		case R.id.stream_to:
			/*
			// TODO: indiquer le nom de l'activité à lancer
			Intent intent = new Intent(getActivity(), GNAGNARD_ACTIVITY.class);
			startActivity(intent);
			*/
			break;
		}
	}

}
