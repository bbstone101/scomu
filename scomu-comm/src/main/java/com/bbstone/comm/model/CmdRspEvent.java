package com.bbstone.comm.model;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

@Data
public class CmdRspEvent {
	@JSONField(ordinal = 1)
	private String id;
	
	@JSONField(ordinal = 2)
	private String cmd;
	
	@JSONField(ordinal = 3)
	private int retCode;
	
	@JSONField(ordinal = 4)
	private String retMsg;
	
	/** json string format of response data */
	@JSONField(ordinal = 5)
	private String retData;
	
	@JSONField(ordinal = 6)
	private long reqTs;
	
	@JSONField(ordinal = 7)
	private long recvTs;
	
	@JSONField(ordinal = 8)
	private long rspTs;
	
	@JSONField(ordinal = 9)
	private String connId;
	
	@JSONField(ordinal = 10)
	private String accessToken;
	
	@JSONField(ordinal = 11)
	private long createTs;
	
	public CmdRspEvent() {}
	
	public CmdRspEvent(String id, String cmd, int retCode, String retMsg, String retData,
			long reqTs, long recvTs, long rspTs, String connId, String accessToken) {
		super();
		this.id = id;
		this.cmd = cmd;
		this.retCode = retCode;
		this.retMsg = retMsg;
		this.retData = retData;
		
		this.reqTs = reqTs;
		this.recvTs = recvTs;
		this.rspTs = rspTs;
		this.connId = connId;
		this.accessToken = accessToken;
		
	}
	
	public static CmdRspEvent from(String id, String cmd, int retCode, String retMsg, String retData,
			long reqTs, long recvTs, long rspTs, String connId, String accessToken) {
		return new CmdRspEvent(id, cmd, retCode, retMsg, retData, reqTs, recvTs, rspTs, connId, accessToken);
	}
	
}
