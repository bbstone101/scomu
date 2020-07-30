package com.bbstone.client.core.model;

public enum ConnStatus {
	
	CONNECTED, 
	
	/** never connected after client startup */
	NOT_CONNECTED, 
	
	/** at least connected for once, but now is disconnected */
	DISCONNECTED, 
	
	FAIL, 
	
	AUTH_FAIL, 
	
	FAKE_SERVER, 
	
	CONNECTING;
}
