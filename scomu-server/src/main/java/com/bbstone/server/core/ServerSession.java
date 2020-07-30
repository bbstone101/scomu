package com.bbstone.server.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.bbstone.comm.model.ConnInfo;
import com.bbstone.server.core.base.Server;
import com.bbstone.server.core.base.ServerConfig;
import com.bbstone.server.core.model.ConnStatus;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * client-server session manager
 * 
 * 
 * @author bbstone
 *
 */
@Slf4j
public class ServerSession {
	
	
	/** client <connId, ConnInfo> */
	private static ConcurrentMap<String, ConnInfo> connInfos = new ConcurrentHashMap<>();
	
	/** client connection status <connId, ConnStatus> */
	private static ConcurrentMap<String, ConnStatus> connStatus = new ConcurrentHashMap<>();
	
	/** client channels <connId, SocketChannel> */
	private static Map<String, SocketChannel> clientChannels = new HashMap<>();
	
	/** server nodes <serverId, Bootstrap> */
	private static Map<String, Server> serverNodes = new HashMap<>();
	
	
	public static void cleanClientSession(String connId) {
		clientChannels.remove(connId);
		connStatus.remove(connId);
		connInfos.remove(connId);
		log.info("conn session is cleaned");
	}
	
	public static void cleanServerSession(String serverId) {
		serverNodes.remove(serverId);
	}
	
	public static void saveConnInfo(ConnInfo connInfo) {
		connInfos.put(connInfo.connId(), connInfo);
	}
	
	public static ConnInfo getConnInfo(String connId) {
		return connInfos.get(connId);
	}
	
	
	public static void saveBootstrap(String connId, Server client) {
		serverNodes.put(connId, client);
	}
	
	public static Server getBootstrap(String connId) {
		return serverNodes.get(connId);
	}
	
	public static void saveSocketChannel(String connId, SocketChannel channel) {
		clientChannels.put(connId, channel);
	}
	
	public static SocketChannel getSocketChannel(String connId) {
		return clientChannels.get(connId);
	}
	

	public static void updateConnStatus(String connId, ConnStatus status) {
		connStatus.put(connId, status);
	}
	
	public static ConnStatus getConnStatus(String connId) {
		return connStatus.get(connId);
	}
	
	public static void startServer() {
		Server bootstrap = new Server();
		bootstrap.startup(ServerConfig.host, ServerConfig.port);
		saveBootstrap(ServerConfig.serverId, bootstrap);
		updateConnStatus(ServerConfig.serverId, ConnStatus.CONNECTING);
		
		log.info("client bootstrap starting ...");
	}
	
	public static void shutdownServer(String serverId) {
		serverNodes.get(serverId).shutdown();
		cleanServerSession(serverId);
	}
	
	public static void closeClientConnection(String connId) {
		clientChannels.get(connId).closeFuture().addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
					log.info("channel is closed.");
				} else {
					log.info("channel close fail.");
				}
			}
			
		});
	}
	

}
