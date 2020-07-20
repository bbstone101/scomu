package com.bbstone.server.core;

import lombok.Data;

@Data
public class Client {
	
	// identify one client->Server connection, aka clientId
	private String connId;
	
	// connect to current server node's user account
	private String username;
	
	private long connTs;
	
	private long authTs;
	
	private long disconnTs;
	
	// reconnect times
	private int reconnTimes;

}
