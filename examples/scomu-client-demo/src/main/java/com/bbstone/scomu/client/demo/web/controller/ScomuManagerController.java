package com.bbstone.scomu.client.demo.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bbstone.client.api.ClientAPI;
import com.bbstone.client.core.ClientConnectionManager;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.util.ConnUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ScomuManagerController {

	@GetMapping("/scomu/start")
	public String startServer() {
//		ConnInfo connInfo = ConnUtil.from("127.0.0.1", 8899, "demo", "demopass");
//		ClientConnectionManager.open(new ConnInfo[] {
//				connInfo
//		});

		// --------
		int clients = 10;
		List<ConnInfo> connInfos = new ArrayList<>();
		for (int i = 0; i < clients; i++) {
			ConnInfo connInfo = ConnUtil.from("127.0.0.1", 8899, "demo", "demopass");
			connInfos.add(connInfo);
		}
		ClientConnectionManager.open(connInfos.toArray(new ConnInfo[connInfos.size()]));
		// ------
		log.info("started {} clients. ", clients);
		return "started ";
	}

	@GetMapping("/scomu/stop")
	public String stopSever(@RequestParam("connId") String connId) {

		boolean connected = ClientConnectionManager.isOpen(connId);
		log.info("check if client connected: {} ", connected);
		if (connected) {
			ClientConnectionManager.close(connId);
			log.info("disconnect by client api");
		} else {
			return "sever is not running.";
		}
		return "stoped";
	}

	@GetMapping("/scomu/test")
	public String test() {
//		int iTask = 1000;
//		for (int i = 0; i < iTask; i++) {
//			log.info("raise request {}", (i+1));
//			ClientAPI.getOrder("1");
//		}

		int iTask = 1000;
		CountDownLatch cdl = new CountDownLatch(iTask);
		ExecutorService es = Executors.newFixedThreadPool(iTask);
		for (int i = 0; i < iTask; i++) {
			log.info("[{}] - added cmd get_order task", (i + 1));
			es.submit(new Runnable() {
				public void run() {
					log.info("in thread.run() method");
					try {
						cdl.await();
						ClientAPI.getOrder("1");
					} catch (InterruptedException e) {
						log.error("cdl await error.", e);
					}
				}
			});
			cdl.countDown();
		}
		log.info("complete all.");
		return "testing.....";
	}

}
