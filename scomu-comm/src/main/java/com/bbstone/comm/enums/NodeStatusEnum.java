package com.bbstone.comm.enums;

public enum NodeStatusEnum {
	
	
	UNKNOWN(-1, "unknown", "未知"),
	CONNECTED(1, "connected", "已连接"),
	AUTH(2, "auth OK", "授权认证通过"),
	ACCEPTABLE(3, "acceptable", "可以接收请求"),
	DIE(4, "die", "故障");
	
	
	private int code;
	private String msg;
	private String msgCn;
	
	NodeStatusEnum(int code, String msg, String msgCn)  {
		this.code = code;
		this.msg = msg;
		this.msgCn = msgCn;
	}

	public int code() {
		return code;
	}
	
	public String msg() {
		return msg;
	}
	
	public String msgCn() {
		return msgCn;
	}
	
	
	
}
