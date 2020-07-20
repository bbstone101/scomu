package com.bbstone.scomu.client.demo.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbstone.client.core.ConnectionManager;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.util.ConnUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ScomuManagerController {

	@GetMapping("/scomu/start")
	public String startServer() {
		ConnInfo connInfo = ConnUtil.from("127.0.0.1", 8899, "demo", "demopass");
		ConnectionManager.open(new ConnInfo[] {
				connInfo
		});
		
		return "started. " + connInfo.connId();
	}
	
	
	@GetMapping("/scomu/stop")
	public String stopSever(@RequestParam("connId") String connId) {
		
		boolean connected = ConnectionManager.isOpen(connId);
		log.info("check if client connected: {} ", connected);
		if (connected) {
			ConnectionManager.close(connId);
			log.info("disconnect by client api");
		} else {
			return "sever is not running.";
		}
		return "stoped";
	}
	
	
}
