package com.bbstone.client.core;

import java.util.ArrayList;
import java.util.List;

import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.comm.model.ConnInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Client Connection Manager
 * 
 *
 * @author bbstone
 *
 */
@Slf4j
public class ClientConnectionManager {
	
//	public static ClientContext open(ConnInfo connInfo) {
//		log.info("open new connection. connId: {}", connInfo.connId());
//		ClientConnector clientConnector = ClientContextHolder.initConnector(connInfo.connId());
//		// new & save context
//		ClientContext clientContext = ClientContextHolder.newContext(connInfo);
//		// connect server
//		clientConnector.connect(clientContext);
//		
//		return clientContext;
//	}
//
//	public static ClientContext[] open(ConnInfo[] connInfos) {
//		List<ClientContext> contexts = new ArrayList<>();
//		for (ConnInfo connInfo : connInfos) {
//			contexts.add(open(connInfo));
//		}
//		return contexts.toArray(new ClientContext[contexts.size()]);
//	}
	
	
	public static ClientContext initialContext(ConnInfo connInfo) {
		log.info("initial a new client context. connId: {}", connInfo.connId());
		// new & save context
		ClientContext clientContext = ClientContextHolder.newContext(connInfo);
		return clientContext;
	}
	
	public static void connect(ConnInfo connInfo, ClientContext clientContext) {
		ClientConnector clientConnector = ClientContextHolder.initConnector(connInfo.connId());
		clientConnector.connect(clientContext);
	}
	
	public static ClientContext initialContextAndConnect(ConnInfo connInfo) {
		ClientContext clientContext = initialContext(connInfo);
		connect(connInfo, clientContext);
		return clientContext;
	}


	public static boolean isOpen(String connId) {
		return ConnStatus.CONNECTED == ClientContextHolder.getContext(connId).getConnStatus();
	}

	public static void close(String connId) {
		ClientContextHolder.getClientConnector(connId).disconnect();
		// channel inactive will deploy and clear context/session
//		ClientContextHolder.getContext(connId).destroy();
	}

	public static void reopen(ConnInfo connInfo) {
		close(connInfo.connId());
		initialContextAndConnect(connInfo);
	}


}
