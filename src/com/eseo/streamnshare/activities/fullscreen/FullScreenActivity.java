package com.eseo.streamnshare.activities.fullscreen;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.eseo.streamnshare.R;
import com.eseo.streamnshare.activities.main.PlayMode;
import com.eseo.streamnshare.fragments.fullscreen.NormalFragment;
import com.eseo.streamnshare.fragments.fullscreen.SinkFragment;
import com.eseo.streamnshare.fragments.fullscreen.SourceFragment;

public class FullScreenActivity extends FragmentActivity{

	private PlayMode mode ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		final ActionBar actionBar = getActionBar();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC); 

		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));


		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Bundle b = getIntent().getExtras();

		mode = (PlayMode)b.getSerializable("mode");
		b.remove("mode");
		
		Fragment fragment = null ;
		switch(mode){
		case NORMAL :
			fragment = new NormalFragment();
			break;
		case SINK :
			fragment = new SinkFragment();
			break;
		case SOURCE :
			fragment = new SourceFragment();
			break;
		}
		ft.replace(R.id.fullscreen_fragment_placeholder, fragment);
		ft.commit();

	}

}
