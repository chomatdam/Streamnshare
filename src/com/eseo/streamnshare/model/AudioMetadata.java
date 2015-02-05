package com.eseo.streamnshare.model;

import org.alljoyn.bus.annotation.Position;

public class AudioMetadata {
	
	@Position(0)
	public String albumName ;
	@Position(1)
	public String artistName;
	@Position(2)
	public String songName ;
	@Position(3)
	public long duration ;
	
}
