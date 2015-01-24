package com.eseo.streamnshare.screens;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

public class MusicController extends MediaController{
	
	public MusicController(Context c){
		super(c);
		  }
	
	public MusicController(Context c, Boolean useFastForward){
		super(c,useFastForward);
		  }
	
	@Override	
	public void hide(){
	}
	/*
	@Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            ((Activity) getContext()).finish();

        return super.dispatchKeyEvent(event);
    }
    */
	
}
