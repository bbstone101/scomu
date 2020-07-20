package com.bbstone.client.core.model;

import lombok.Data;

@Data
public class Statis {
	
	/** success connect times */
	private int connectTimes;
	
	/** is client current connected */
	private boolean isConnected;
	
	/** is client ready to accept command */
	private boolean isCmdAcceptable;
	
	/** is channel active now */
	private boolean isChannelActived;
	
	/** reconnect retry times, after connection established, reset to 0 */
	private int retryTimes;

}
