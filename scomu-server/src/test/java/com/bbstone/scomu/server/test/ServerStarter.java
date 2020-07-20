package com.bbstone.scomu.server.test;

import com.bbstone.server.core.ServerManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerStarter {

	public static void main(String[] args) throws Exception {
		ServerManager.startServer();
		
		log.info("server bootstrap started...");
	}

}
