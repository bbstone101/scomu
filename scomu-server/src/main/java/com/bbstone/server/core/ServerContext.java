package com.bbstone.server.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.model.ServerAuthInfo;
import com.bbstone.comm.util.TokenUtil;

/**
 * 
 * Server Context
 * 
 * 
 * @author bbstone
 *
 */
public class ServerContext {

//	private static ConcurrentMap<String, Node> nodes = new ConcurrentHashMap<>();

	private static ConcurrentMap<String, MessageHandlerRegister> cmdRegister = new ConcurrentHashMap<>();
	private static ConcurrentMap<String, ServerAuthInfo> serverAuthInfos = new ConcurrentHashMap<>();

	// <connId, Client>
	private static ConcurrentMap<String, Client> clients = new ConcurrentHashMap<>();

	/**
	 * every running cmd will store here, cmd will be deleted in this case: 1) cmd
	 * rsp;2)timeout;3)crash(re-initial)
	 */
	private static ConcurrentMap<String, CmdReqEvent> runningReqCmds = new ConcurrentHashMap<>();

	private static String nodeId = TokenUtil.UUID32();

	/** server nodes <serverId, Bootstrap> */
	private static Map<String, Server> servers = new HashMap<>();

	/** client connection status <connId, ServerStatus> */
	private static ConcurrentMap<String, ServerStatus> serverStatus = new ConcurrentHashMap<>();

	/** clear current node context */
	public static void clearMe(String nodeId) {
		servers.remove(nodeId);
		serverStatus.clear();
		cmdRegister.remove(getNodeId());
		serverAuthInfos.remove(getNodeId());
		clients.remove(getNodeId());
		runningReqCmds.remove(getNodeId());

	}

	/** clear all nodes context */
	public static void clearAllNodes() {
		servers.clear();;
		serverStatus.clear();
		clients.clear();
		cmdRegister.clear();
		runningReqCmds.clear();
		serverAuthInfos.clear();
	}
	

	public static void saveServer(String serverId, Server server) {
		servers.put(serverId, server);
	}

	public static Server getServer(String serverId) {
		return servers.get(serverId);
	}

	public static void updateServerStatus(String serverId, ServerStatus status) {
		serverStatus.put(serverId, status);
	}

	public static ServerStatus getServerStatus(String serverId) {
		return serverStatus.get(serverId);
	}


	public static void saveMsgHandlerRegister(MessageHandlerRegister register) {
		cmdRegister.put(getNodeId(), register);
	}

	public static MessageHandlerRegister getMsgHandlerRegister() {
		return cmdRegister.get(getNodeId());
	}

	public static void putCmdReqEvent(CmdReqEvent cmdReqEvent) {
		runningReqCmds.put(cmdReqEvent.getId(), cmdReqEvent);
	}

	public static CmdReqEvent getCmdReqEvent(String id) {
		return runningReqCmds.get(id);
	}

	public static void removeCmdReqEvent(String id) {
		runningReqCmds.remove(id);
	}

	public static void saveServerAuthInfo(String connId, ServerAuthInfo serverAuthInfo) {
		serverAuthInfos.put(connId, serverAuthInfo);
	}

	public static ServerAuthInfo getServerAuthInfo(String connId) {
		if (serverAuthInfos.get(connId) == null) {
			serverAuthInfos.put(connId, new ServerAuthInfo(connId));
		}
		return serverAuthInfos.get(connId);
	}

	public static void removeServerAuthInfo(String connId) {
		serverAuthInfos.remove(connId);
	}

	public static List<String> freeAccessCmds() {
		return Arrays.asList(StringUtils.split(ServerConfig.free_access_cmds, ","));
	}

	public static String getNodeId() {
		return nodeId;
	}

	public static void addClient(String connId, Client client) {
		clients.put(connId, client);
	}

	public static Client getClient(String connId) {
		return clients.get(connId);
	}

}
