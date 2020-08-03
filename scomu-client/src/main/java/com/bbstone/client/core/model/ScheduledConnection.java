package com.bbstone.client.core.model;

import lombok.Data;

@Data
public class ScheduledConnection {
	private String connId;
	private long createTime;
	private int delay;
	private int retryTimes;
	
	

}
