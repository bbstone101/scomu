package com.bbstone.scomu.client.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bbstone.client.api.ClientAPI;
import com.bbstone.client.core.ClientConnectionManager;
import com.bbstone.client.core.ClientContext;
import com.bbstone.client.core.ClientContextHolder;
import com.bbstone.client.core.ClientUtil;
import com.bbstone.client.core.ext.AuthSuccessListener;
import com.bbstone.client.core.ext.SpeedChangeListner;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.util.ConnUtil;

import io.netty.channel.ChannelHandlerContext;
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
		for (int i = 0; i < clients; i++ ) {
			ConnInfo connInfo = ConnUtil.from("127.0.0.1", 8899, "demo", "demopass");
			ClientContext clientContext = ClientConnectionManager.initialContext(connInfo);
			ClientContextHolder.getContext(connInfo.getConnId()).addAuthSuccessListener(new AuthSuccessListener() {
				public void invoke(ChannelHandlerContext ctx) {
					ClientUtil.addSpeedChangeListner(connInfo.getConnId(), new SpeedChangeListner() {
						public void onInputSpeedChange(String connId, long speed, String speedUnit) {
							log.info("======== input speed change =========");
							log.info("connId: {}, {}{}", connId, speed, speedUnit);
							log.info("======== input speed change =========");
							
						}
						public void onOuputSpeedChange(String connId, long speed, String speedUnit) {
							log.info("======== output speed change =========");
							log.info("connId: {}, {}{}", connId, speed, speedUnit);
							log.info("======== output speed change =========");
						}
					});
				}
			});
			ClientConnectionManager.connect(connInfo, clientContext);
		}
//		List<ConnInfo> connInfos = new ArrayList<>();
//		for (int i = 0; i < clients; i++ ) {
//			ConnInfo connInfo = ConnUtil.from("127.0.0.1", 8899, "demo", "demopass");
//			connInfos.add(connInfo);
//		}
//		ClientConnectionManager.open(connInfos.toArray(new ConnInfo[connInfos.size()]));
		
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
