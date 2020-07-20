package com.bbstone.client.util;

import com.bbstone.client.core.ClientSession;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.util.TokenUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientUtil {

	
	/**
	 * generate cliRand for cmd(AuthAnswer)
	 * 
	 * @return
	 */
	public static String genClientRand() {
		return TokenUtil.UUID32();
	}

	// -------------- CmdEvent req/rsp timeout
	// CmdEvent created -> enqueue(req) -> outqueue(req) -> send2mt5 -> mt5Rsp ->
	// enqueue(rsp) -> outqueue(rsp) -> client code|end

	/**
	 * check if CmdEvent timeout before consume from reqQueue (default 2000ms)
	 * 
	 * @param cmdReqEvent
	 * @return
	 */
	public static boolean isReqTimeout(CmdReqEvent cmdReqEvent) {
		return (System.currentTimeMillis() - cmdReqEvent.getCreateTs() >= ClientSession.CMD_REQ_TIMEOUT);
	}


	/**
	 * CommandConsumerTask idle max total timeout, more than CmdRspTimeout(5s) one
	 * idle time (default 500ms), so default is 5.5s
	 * 
	 * @param cmdEvent
	 * @return
	 */
	public static boolean isCmdTimeout(CmdReqEvent cmdReqEvent) {
		if (cmdReqEvent == null || cmdReqEvent.getCreateTs() == 0) {
			log.error("cmdEvent is null or createTime not set.");
			return true;
		}
		return (System.currentTimeMillis() - cmdReqEvent.getCreateTs() >= ClientSession.REQ_CONSUME_IDLE_TIME_MAX);
	}
	

}
