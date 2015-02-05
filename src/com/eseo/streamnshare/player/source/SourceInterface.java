package com.eseo.streamnshare.player.source;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;

import com.eseo.streamnshare.model.AudioMetadata;
import com.eseo.streamnshare.model.AudioTrackConfig;
import com.eseo.streamnshare.player.StatesPlayer;

@BusInterface(name="com.eseo.streamnshare.player.source")
public interface SourceInterface {

	@BusSignal
	public void signalAudioData(byte[] audioData, int position) throws BusException ;
	
	@BusSignal(signature = "r")
	public void signalAudioTrackConfig(AudioTrackConfig config) throws BusException ;
	
	@BusSignal(signature = "r")
	public void signalMetadata(AudioMetadata metadata) throws BusException ;
	
	@BusSignal(signature = "i")
	public void signalStatePlayer(StatesPlayer state) throws BusException ;
	
	
}
