package com.eseo.streamnshare.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eseo.streamnshare.R;

/* Adapters: https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView */
public class ArtistAdapter extends ArrayAdapter<String> {

	public ArtistAdapter(Context c, ArrayList<String> songList){
		super(c,0,songList);
	}

	static class ViewHolderItem {
		TextView songArtistItem;
	}

	/* ViewHolder pattern: https://www.codeofaninja.com/2013/09/android-viewholder-pattern-example.html */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolderItem viewHolder;

		if (convertView == null) {
			// Inflate layout first time
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist_list_item, parent, false);

			// well set up the ViewHolder (partie où sans ViewHolder, cela est coûteux en perf)
			viewHolder = new ViewHolderItem();
			viewHolder.songArtistItem = (TextView)convertView.findViewById(R.id.song_artist);

			// store the holder with the view
			convertView.setTag(viewHolder);
		}else{
			// using the viewHolder instead of calling findViewById() on resource everytime
			viewHolder = (ViewHolderItem) convertView.getTag();
		}

		// song object based on the position
		String artist = getItem(position);

		if(artist != null){
			viewHolder.songArtistItem.setText(artist);
		}

		return convertView;
	}

}