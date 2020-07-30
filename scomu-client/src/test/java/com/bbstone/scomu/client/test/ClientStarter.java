package com.bbstone.scomu.client.test;

import com.bbstone.client.core.AuthSuccessListener;
import com.bbstone.client.core.ClientContextHolder;
import com.bbstone.client.core.ConnectionManager;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.util.ConnUtil;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientStarter {

	public static void main(String[] args) throws Exception {
//		ConnInfo connInfo = ConnUtil.from("127.0.0.1", 8899, "demo3", "demopass");
//		ConnectionManager.open(connInfo);
//		ClientContextHolder.getClientSession(connInfo.connId()).addAuthSuccessListener(new AuthSuccessListener() {
//			public void invoke(ChannelHandlerContext ctx) {
//				log.info("auth success call back listener invoking.......");
//				
//			}
//		});
////		ClientAPI.getOrder("1");
//		log.info("complete........");
//		
////		ThreadViewer.showThreads();
		
		
		ConnInfo connInfo = ConnUtil.from("127.0.0.1", 8899, "demo", "demopass");
		ConnectionManager.open(new ConnInfo[] {
				connInfo
		});
	}

}
