package com.bbstone.comm.util;

import com.bbstone.comm.model.ConnInfo;

/**
 * 
 *
 * @author bbstone
 *
 */
public class ConnUtil {
	
	public static ConnInfo from(String host, int port, String username, String password) {
		return new ConnInfo(host, port, username, password);
	}
	
	

}
