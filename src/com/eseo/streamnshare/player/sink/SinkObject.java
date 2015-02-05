package com.eseo.streamnshare.player.sink;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;

public class SinkObject implements BusObject, SinkInterface {

	@Override
	public void signalFifo() throws BusException {}



}
