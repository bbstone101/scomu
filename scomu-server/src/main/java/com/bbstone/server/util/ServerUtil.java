package com.bbstone.server.util;

import org.apache.commons.lang3.StringUtils;

import com.bbstone.comm.model.ServerAuthInfo;
import com.bbstone.comm.util.CmdUtil;
import com.bbstone.comm.util.TokenUtil;
import com.bbstone.server.core.ServerConfig;
import com.bbstone.server.core.ServerContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerUtil {

//	public static void startServer() {
//		ServerManager.startServer();
//	}
//
//	public static void stopServer(String serverId) {
//		ServerManager.shutdownServer(serverId);
//	}
//
//	public static void restartServer(String serverId) {
//		ServerManager.shutdownServer(serverId);
//		ServerManager.startServer();
//	}
//
//	public static boolean isConnected(String connId) {
//		return ConnStatus.CONNECTED == ServerSession.getConnStatus(connId);
//	}
//
//	public static void closeClient(String connId) {
//		ServerSession.closeClientConnection(connId);
//	}
	
	
	
	
	public static String genSrvRand() {
		return TokenUtil.UUID32();
	}
	
	public static String genAccessToken() {
		return TokenUtil.UUID32();
	}
	
	
	public static boolean checkSrvRandAnswer(String connId, String srvRandAnswer) {
		if (StringUtils.isBlank(srvRandAnswer)) return false;
		
		ServerAuthInfo authInfo = ServerContext.getServerAuthInfo(connId);
		String password = findPassword(authInfo.getAuthStartReq().getUsername());
		if (StringUtils.isBlank(password)) return false;
		
		String srvRandAnswerCalc = CmdUtil.calcSrvRandAnswer(authInfo.getSrvRand(), password);
		return srvRandAnswer.equals(srvRandAnswerCalc);
	}
	
	public static String findPassword(String username) {
		if ("demo".equals(ServerConfig.authDB)) {
			if (ServerConfig.demoUsername.equals(username)) {
				return ServerConfig.demoPassword;
			} else {
				log.error("cannot found demo password for user: {}.", username);
				throw new RuntimeException("demo username/password error.");
			}
		} 
		if ("redis".equals(ServerConfig.authDB)) {
			// TODO read password from redis/mysql ..
			
		}
		return null;
	}

}
