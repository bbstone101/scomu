package com.bbstone.comm.model;

import com.bbstone.comm.enums.NodeStatusEnum;

import lombok.Data;

@Data
public class NodeStats {

	private String id;
	private NodeStatusEnum status;
	
	private int processThreads;
	
	private int failReqs;
	private int successReqs;
	private int waitingReqs;
	private int processingReqs;

	
	private int reconnTimes;
	private int lostConnTimes;
	
	public NodeStats() {}
	
	public NodeStats(String id, NodeStatusEnum status) {
		this.id = id;
		this.status = status;
	}
	
}
