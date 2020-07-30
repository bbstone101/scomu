package com.bbstone.comm.enums;

/**
 * communication command
 * 
 * most commands are request by client, but some raise request from server, e.g. STOP_RECONN,
 * 
 * it has reason, when server detect client heat-beat timeout, need to active close connection retry 
 * 
 * @author bbstone
 *
 */
public enum CC {
	
	// client -> server
	AUTH_START(101, "step1: send auth request to server"),
	AUTH_ANSWER(102, "step2: send auth answer to sever with server random cod answer"),
	
	HEART_BEAT(0, "heart beat"),
	TEST_ACCESS(1, "test access"),
	
	// server -> client cmd
	SRV_CMD_STOP_RECONN(501, "stop client connection retry"),
	
	
	
	
	GET_ORDER(1001, "get order by id"),
	
	
	
	
	
	
	
	
	OTHER(99999, "other");
	
	
	private int code;
	private String descp;
	
	CC(int code, String descp) {
		this.code = code;
		this.descp = descp;
	}
	
	public int code() {
		return this.code;
	}
	
	public String descp() {
		return this.descp;
	}

}
