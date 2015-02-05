package com.eseo.streamnshare.fragments.main;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.adapters.ArtistAdapter;
import com.eseo.streamnshare.managers.MusicManager;
import com.eseo.streamnshare.model.Song;

public class ArtistListFragment extends ListFragment{

	ArrayList<Song> songList;
	ArrayList<String> artistList;
	HashMap<String,ArrayList<Song>> map;
	OnItemSelectedFragmentListener listener;

	public ArtistListFragment(){
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		songList = MusicManager.getInstance(getActivity()).getSongList();
        map = MusicManager.getInstance(getActivity()).getArtistList();
        artistList = new ArrayList<String>();
        for (String artist : map.keySet())  
        	artistList.add(artist);
		MusicManager.getInstance(getActivity()).sortByArtist(artistList);
		ArtistAdapter adapter = new ArtistAdapter(getActivity(),artistList);
		setListAdapter(adapter);
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
		return inflater.inflate(R.layout.fragment_list, container, false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		String artist = artistList.get(l.getPositionForView(v));
		songList = map.get(artist);
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_placeholder, SongListFragment.newInstance(songList));
		ft.commit();
	}
}
