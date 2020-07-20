package com.bbstone.server.core;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ServerManager {

	
	public static String startServer() {
		Server bootstrap = new Server();
		bootstrap.startup(ServerConfig.host, ServerConfig.port);
		ServerContext.saveServer(ServerContext.getNodeId(), bootstrap);
		ServerContext.updateServerStatus(ServerContext.getNodeId(), ServerStatus.STARTING);
		log.info("server bootstrap starting ...");
		return ServerContext.getNodeId();
	}

	public static void shutdownServer(String nodeId) {
		ServerContext.getServer(nodeId).shutdown();
		ServerContext.clearMe(nodeId);
		log.info("server now is shutting down.");
	}
	
	public static void restartServer(String nodeId) {
		shutdownServer(nodeId);
		startServer();
	}
	
}
