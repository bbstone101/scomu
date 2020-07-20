package com.bbstone.comm.model;

import com.bbstone.comm.util.TokenUtil;

import lombok.Data;

@Data
public class ConnInfo {
	
	private int port;
	
	private String host;
	
	private String username;
	
	/** 
	 * password will not pass through network,
	 * this field not store plain text, but sha256(password_plaintext),
	 * e.g. if user alpha's password is 12345, the password field store: sha256(12345)
	 *  */
	private String password;
	
	public ConnInfo() {}

	public ConnInfo(String host, int port, String username, String password) {
		super();
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	private String connId = TokenUtil.UUID32();
	
	/**
	 *  every connection has a unique connId(clientId)
	 *  
	 * @return
	 */
	public String connId() {
		return connId;
	}
	
}
