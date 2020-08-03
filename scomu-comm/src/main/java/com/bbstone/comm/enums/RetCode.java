package com.bbstone.comm.enums;

public enum RetCode {
	
	SUCCESS(0, "success"),
	FAIL(1, "fail"),
	THREAD_INTERRUPTED(2, "Thread has been interrupted."),
	REQ_TIMEOUT(3, "Request timeout."),
	
	
	
	OTHER(9999, "other");

	
	private int code;
	private String descp;
	
	
	RetCode(int code, String descp) {
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
