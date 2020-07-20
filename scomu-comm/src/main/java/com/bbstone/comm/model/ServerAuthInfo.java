package com.bbstone.comm.model;

import com.bbstone.comm.dto.req.AuthStartReq;

import lombok.Data;

@Data
public class ServerAuthInfo {
	
	private AuthStartReq authStartReq;
	
	
	// md5(connInfo.ip+connInfo.port+connInfo.username)
	private String connId;
	
	// R2: server generated random code
	private String srvRand;
	
	/**
	 * used for double side authentication
	 * 
	 * client used srvRand and client provided password to calc srvRandAnswer, 
	 * server will calc the srvRandAnswer again with the username's password store in server side,
	 * if client srvRandAnswer same as server calc value, this field has value 
	 */
	private String srvRandAnswer;
	
	/**
	 * after srvRandAnswer field set, server generate accessToken for client
	 */
	private String accessToken;
	
	public ServerAuthInfo() {}
	
	public ServerAuthInfo(String connId) {
		this.connId = connId;
	}

}
