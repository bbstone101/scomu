package com.bbstone.client.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.bbstone.client.core.base.Client;
import com.bbstone.client.core.base.Connector;
import com.bbstone.client.core.base.MessageDispatcher;

public class ClientContextHolder {

	private static Map<String, ClientContext> clientContexts = new HashMap<>();

	public static void newContext(String connId) {
		if (clientContexts.get(connId) == null)
			clientContexts.put(connId, new ClientContext(connId));
	}

	public static ClientContext getContext(String connId) {
		return clientContexts.get(connId);
	}
	
	public static ClientContext selectContext() {
		// TODO implement client load balancer algo
		for (Iterator<String> it = clientContexts.keySet().iterator(); it.hasNext(); ) {
			return clientContexts.get(it.next());
		}
		return null;
	}

	// --------------
	public static Connector getClientConnector(String connId) {
		return getContext(connId).getClientConnector();
	}
	public static Client getClientConnection(String connId) {
		return getClientSession(connId).getConnection();
	}
	public static ClientProcessor getClientProcessor(String connId) {
		return getContext(connId).getClientProcessor();
	}

	public static ClientSession getClientSession(String connId) {
		return getContext(connId).getClientSession();
	}

	public static CmdReqBuilder getCmdReqBuilder(String connId) {
		return getContext(connId).getCmdReqBuilder();
	}

	public static MessageDispatcher getMessageDispatcher(String connId) {
		return getContext(connId).getMessageDispatcher();
	}

	public static MessageHandlerRegister getMessageHandlerRegister(String connId) {
		return getContext(connId).getMessageHandlerRegister();
	}

	public static HeartBeatExecutor getHeartBeatExecutor(String connId) {
		return getContext(connId).getHeartBeatExecutor();
	}

}
