package com.eseo.streamnshare.player;

import android.os.Handler;
import android.os.Message;

import com.eseo.streamnshare.model.Song;

public class HandlerPlayer extends Handler{

	private Player player ;
	private EventPlayer events ;

	public HandlerPlayer(Player player) {
		this.player = player ;
	}

	public HandlerPlayer(Player player, EventPlayer events) {
		this.player = player ;
		this.events = events;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if(events != null) {
			switch(msg.what){
			case EventPlayer.PLAY : 
				events.onPlay();
				break ;
			case EventPlayer.START : 
				Song song = (Song) msg.obj ;
				events.onStart(song);
				break ;
			case EventPlayer.STOP : 
				events.onStop();
				break ;
			case EventPlayer.SEEK : 
				int percent = (Integer) msg.obj ;
				events.onSeek(percent);
				break ;
			case EventPlayer.ERROR : 
				events.onError();
				player.setError(true);
				break ;
			}
		}
	}

	public EventPlayer getEvents() {
		return events;
	}

	public void setEvents(EventPlayer events) {
		this.events = events;
	}
}
