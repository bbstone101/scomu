package com.bbstone.scomu.client.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bbstone.client.api.ClientAPI;
import com.bbstone.client.core.ClientConnectionManager;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.util.ConnUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientStarter {

	public static void main(String[] args) throws Exception {
//		ConnInfo connInfo = ConnUtil.from("127.0.0.1", 8899, "demo", "demopass");
//		ClientContext clientContext = ClientConnectionManager.open(connInfo);
//		clientContext.addAuthSuccessListener(new AuthSuccessListener() {
//			public void invoke(ChannelHandlerContext ctx) {
//				log.info("auth success call back listener invoking.......");
//				
//			}
//		});
//		while (!clientContext.isCmdAcceptable()) {
//			Thread.sleep(500);
//		}
// 		ClientAPI.getOrder("1");
//		log.info("complete........");
//		
////		ThreadViewer.showThreads();
		
		int clients = 20;
		List<ConnInfo> connInfos = new ArrayList<>();
		for (int i = 0; i < clients; i++ ) {
			ConnInfo connInfo = ConnUtil.from("127.0.0.1", 8899, "demo", "demopass");
			connInfos.add(connInfo);
		}
		ClientConnectionManager.open(connInfos.toArray(new ConnInfo[connInfos.size()]));
		
		log.info("client open {} client connections.", clients);
		Thread.sleep(2000);
		int iTask = 100_000;
		CountDownLatch cdl = new CountDownLatch(iTask);
		ExecutorService es = Executors.newFixedThreadPool(clients);
		for (int i = 0; i < iTask; i++) {
			
			log.info("[{}] - added cmd get_order task", (i+1));
			es.submit(new Runnable() {
				public void run() {
					log.info("in thread.run() method");
					try {
						cdl.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					ClientAPI.getOrder("1");
				}
			});
			cdl.countDown();
			log.info("submit task -> {}.", (i+1));
		}
		log.info("complete all.");
	}

}
