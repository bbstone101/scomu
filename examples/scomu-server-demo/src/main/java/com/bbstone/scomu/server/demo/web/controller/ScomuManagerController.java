package com.bbstone.scomu.server.demo.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbstone.server.core.ServerManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ScomuManagerController {

	@GetMapping("/server/start")
	public String startServer() {
		String nodeId = ServerManager.startServer();
//		ControlCmdExecutor.start();
		log.info("server bootstrap started...");
		return "started. serverId: " + nodeId;
	}

	@GetMapping("/server/stop")
	public String stopSever(@RequestParam("serverId") String serverId) {
		ServerManager.shutdownServer(serverId);
		return "stoped";
	}
	
	@GetMapping("/server/restart")
	public String restartSever(@RequestParam("serverId") String serverId) {
		ServerManager.restartServer(serverId);
		return "restarted";
	}
	

}
