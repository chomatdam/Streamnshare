package com.eseo.streamnshare.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.model.Song;

/* Adapters: https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView */
public class SongAdapter extends ArrayAdapter<Song> {

	public SongAdapter(Context c, ArrayList<Song> songList){
		super(c,0,songList);
	}

	static class ViewHolderItem {
		TextView songTitleItem;
		TextView songArtistItem;
	}

	/* ViewHolder pattern: https://www.codeofaninja.com/2013/09/android-viewholder-pattern-example.html */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolderItem viewHolder;

		if (convertView == null) {
			// Inflate layout first time
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.song_list_item, parent, false);

			// well set up the ViewHolder (partie où sans ViewHolder, cela est coûteux en perf)
			viewHolder = new ViewHolderItem();
			viewHolder.songArtistItem = (TextView)convertView.findViewById(R.id.song_artist);
			viewHolder.songTitleItem = (TextView)convertView.findViewById(R.id.song_title); 

			// store the holder with the view
			convertView.setTag(viewHolder);
		}else{
			// using the viewHolder instead of calling findViewById() on resource everytime
			viewHolder = (ViewHolderItem) convertView.getTag();
		}

		// song object based on the position
		Song currSong = getItem(position);

		if(currSong != null){
			viewHolder.songArtistItem.setText(currSong.getArtist());
			viewHolder.songTitleItem.setText(currSong.getTitle());
		}

		return convertView;
	}

}