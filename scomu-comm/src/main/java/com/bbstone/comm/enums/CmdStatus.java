package com.bbstone.comm.enums;

public enum CmdStatus {
	
	SUCCESS(0, "SUCCESS"),
	FAIL(1, "FAIL"),
	REQ_TIMEOUT(2, "CMD_REQ_TIMEOUT"),
	RSP_TIMEOUT(3, "CMD_RSP_TIMEOUT");
	
	private int code;
	private String msg;
	
	CmdStatus(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public int code() {
		return this.code;
	}
	
	public String msg() {
		return this.msg;
	}
}
