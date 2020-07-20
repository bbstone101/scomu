package com.bbstone.comm.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

@Data
public class CmdResult {

	@JSONField(ordinal = 1)
	public int code;
	
	@JSONField(ordinal = 2)
	public String msg;
	
	@JSONField(ordinal = 3)
	private String data;
	
	
	public CmdResult() {}


	public CmdResult(int code, String msg) {
		super();
		this.code = code;
		this.msg = msg;
	}
	
	public CmdResult(int code, String msg, String data) {
		super();
		this.code = code;
		this.msg = msg;
		this.data = data;
	}
	
	
	public String toString() {
		return JSON.toJSONString(this);
	}
	
	public static CmdResult from(int code, String msg) {
		return new CmdResult(code, msg);
	}
	
	public static CmdResult from(int code, String msg, String data) {
		return new CmdResult(code, msg, data);
	}
}
