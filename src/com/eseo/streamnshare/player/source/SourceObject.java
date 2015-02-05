package com.eseo.streamnshare.player.source;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;

import com.eseo.streamnshare.model.AudioMetadata;
import com.eseo.streamnshare.model.AudioTrackConfig;
import com.eseo.streamnshare.player.StatesPlayer;

public class SourceObject implements BusObject, SourceInterface {

	@Override
	public void signalAudioData(byte[] audioData,int position) throws BusException {}

	@Override
	public void signalAudioTrackConfig(AudioTrackConfig config)throws BusException {}

	@Override
	public void signalMetadata(AudioMetadata metadata) throws BusException {}
	
	@Override
	public void signalStatePlayer(StatesPlayer state) throws BusException {	}

}
