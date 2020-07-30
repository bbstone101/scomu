package com.bbstone.client.core;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.bbstone.comm.enums.CC;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.util.CmdUtil;
import com.bbstone.comm.util.TokenUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * If no packets were received from a client for 120 seconds, the server breaks
 * connection.
 * 
 * Thus, the further execution of commands will be impossible until you complete
 * the authentication procedure.
 * 
 * In order to maintain a connection to the server, a Web client must send empty
 * packets (called "pings") to the server.
 * 
 * NOTICE: send "pings" or TEST_TRADE to server every 20s cannot keep
 * connection, will break every 120s either.
 * 
 * @author bbstone
 *
 */
@Slf4j
public class HeartBeatExecutor {
	
	private boolean switcher = false;
	private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

	// ------------------------
	private long initialDelay = 6L; //60L;
	private long period = 10L; //100L;

	public void keepAlived(String connId) {
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (switcher) {
					sendHb(connId);
				}
			}
		}, initialDelay, period, TimeUnit.SECONDS);
	}

	public void sendHb(String connId) {
		String cmdId = TokenUtil.UUID32();
		CmdReqEvent cmdReqEvent = CmdReqEvent.from(cmdId, CC.HEART_BEAT.name(), 
				CmdUtil.getEmptyReqData(), 
				System.currentTimeMillis(), 
				System.currentTimeMillis(), 
				connId);
		ClientContextHolder.getContext(connId).sendReqOnly(cmdReqEvent);
//		ClientContextHolder.getContext(connId).sendReq(cmdReqEvent);
//		CmdResult cmdReult = ClientUtil.sendReq(cmdReqEvent);
//		System.out.println("<<<<<<<<<<<<" + cmdReult.getCode() + ", " + cmdReult.getMsg() + ">>>>>>>>>>>>>>>>>");
//		CmdResult result = future.getResult();
//		future.addListener(new ResultListner() {
//			@Override
//			public void resultReady(CmdResult cmdReult) {
//				System.out.println("callback message......");
//				System.out.println("<<<<<<<<<<<<" + cmdReult.getCode() + ", " + cmdReult.getMsg() + ">>>>>>>>>>>>>>>>>");
//				
//			}
//		});
		log.info("send {} command, cmdId: {}", CC.HEART_BEAT, cmdId);
	}
	
	public void enableHeartBeat() {
		switcher = true;
	}
	
	public void disableHeartBeat() {
		switcher = false;
	}

}
