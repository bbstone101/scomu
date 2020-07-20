package com.bbstone.comm.model;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;


/**
 * 
 * this CmdReqEvent just for WebApi request, 
 * 
 * api create CmdReqEvent -> CmdReqQueue -> CmdReqEvent transform to CmdReq, 
 * 
 * before send out CmdReq to server, need set accessToken to CmdReq, 
 * 
 * if no accessToken will be reject by server
 * 
 * @author bbstone
 *
 */
@Data
public class CmdReqEvent {
	
	@JSONField(ordinal = 1)
	private String id;
	
	@JSONField(ordinal = 2)
	private String cmd;
	
	@JSONField(ordinal = 3)
	private String data;
	
	@JSONField(ordinal = 4)
	private long reqTs;
	
	@JSONField(ordinal = 5)
	private String connId;
	
	@JSONField(ordinal = 6)
	private long createTs;
	
	public CmdReqEvent() {}

	public CmdReqEvent(String id, String cmd, String data, long createTs, long reqTs, String connId) {
		super();
		this.id = id;
		this.cmd = cmd;
		this.data = data;
		this.createTs = createTs;
		this.reqTs = reqTs;
		this.connId = connId;
	}
	
	public static CmdReqEvent from(String id, String cmd, String data, long createTs, long reqTs, String connId) {
		return new CmdReqEvent(id, cmd, data, createTs, reqTs, connId);
	}
	

}
