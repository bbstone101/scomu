package com.bbstone.client.core;

import java.util.ArrayList;
import java.util.List;

import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.comm.model.ConnInfo;

/**
 * Client Connection Manager
 * 
 *
 * @author bbstone
 *
 */
public class ConnectionManager {

	public static String open(ConnInfo connInfo) {
		ClientContextHolder.newContext(connInfo.connId());
		ClientContextHolder.getClientConnector(connInfo.connId()).connect(connInfo);
		return connInfo.connId();
	}

	public static String[] open(ConnInfo[] connInfos) {
		List<String> connIds = new ArrayList<>();
		for (ConnInfo connInfo : connInfos) {
			connIds.add(open(connInfo));
		}
		return connIds.toArray(new String[connIds.size()]);
	}

	public static boolean isOpen(String connId) {
		return ConnStatus.CONNECTED == ClientContextHolder.getClientSession(connId).getConnStatus();
	}

	public static void close(String connId) {
		ClientContextHolder.getClientConnector(connId).disconnect(connId);
		ClientContextHolder.getContext(connId).destroy();
	}

	public static void reopen(ConnInfo connInfo) {
		close(connInfo.connId());
		open(connInfo);
	}

}
