package com.bbstone.comm.model;

import lombok.Data;

@Data
public class Node {
	
	private String id;
	
	private int port;
	
	private String host;
	
	private boolean isMaster;
	

}
