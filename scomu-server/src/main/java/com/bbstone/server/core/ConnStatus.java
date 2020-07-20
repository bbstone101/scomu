package com.bbstone.server.core;

public enum ConnStatus {
	
	CONNECTED, 
	
	/** never connected after client startup */
	NOT_CONNECTED, 
	
	/** at least connected for once, but now is disconnected */
	DISCONNECTED, 
	
	CONNECTING;
}
