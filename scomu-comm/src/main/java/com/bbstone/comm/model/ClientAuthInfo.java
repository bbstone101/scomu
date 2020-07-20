package com.bbstone.comm.model;

import com.bbstone.comm.dto.req.AuthStartReq;

import lombok.Data;

@Data
public class ClientAuthInfo {
	
	private AuthStartReq authStartReq;
	
	// md5(connInfo.ip+connInfo.port+connInfo.username)
	private String connId;
	
	// R3: client send
	private String cliRand;
	
	/**
	 * used for double side authentication
	 * 
	 * when server receive cliRand, it will used it and user's password(stored in server side) 
	 *  to calculate the cliRandAnser, and send back to client,
	 *  client receive the cliRandAnswer, will calculate a cliRandAnswer with user provided username & password,
	 *  if server side cliRandAnswer same as client side cliRandAnswer, means the server is the right one(not fake server)
	 */
	private String cliRandAnswer;
	
	/**
	 * after cliRandAnswer field set, client store the accessToken for the rest request
	 */
	private String accessToken;
	
	
	public ClientAuthInfo() {}
	
	public ClientAuthInfo(String connId) {
		this.connId = connId;
	}

}
