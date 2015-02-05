package com.eseo.streamnshare.model;

import org.alljoyn.bus.annotation.Position;
import org.alljoyn.bus.annotation.Signature;

import android.media.MediaFormat;

public class AudioTrackConfig {

	@Position(0)
	public String mime ;

	@Position(1)
	public int sampleRate ;

	@Position(2)
	public int channels ;

	@Position(3)
	public long duration ;

}
