package com.eseo.streamnshare.player.sink;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;

@BusInterface(name="com.eseo.streamnshare.player.sink")
public interface SinkInterface {

	@BusSignal
	public void signalFifo() throws BusException ;
	
	
	
}
