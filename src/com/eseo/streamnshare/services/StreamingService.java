package com.eseo.streamnshare.services;

import android.content.Intent;

import com.eseo.streamnshare.model.Song;
import com.eseo.streamnshare.player.EventPlayer;

public abstract class StreamingService extends MusicService{
	
	
	protected EventPlayer events = new EventPlayer() {
		@Override public void onStop() {
			mBroadcastManager.sendBroadcast(new Intent(ON_STOP));
		}
		@Override public void onStart(Song song) {
			Intent intent = new Intent(ON_START);
			intent.putExtra(ON_START,song);
			mBroadcastManager.sendBroadcast(intent);
		}
		@Override public void onSeek(int percent) {
			Intent intent = new Intent(ON_SEEK);
			intent.putExtra(ON_SEEK,percent);
			mBroadcastManager.sendBroadcast(intent);
		}
		@Override public void onPlay() {
		}
		@Override public void onError() {
		}
	};
	

}
