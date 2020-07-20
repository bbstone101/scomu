package com.bbstone.client.core.handler;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.bbstone.client.core.ClientContextHolder;
import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.comm.dto.rsp.AuthAnswerRsp;
import com.bbstone.comm.model.ClientAuthInfo;
import com.bbstone.comm.model.CmdRspEvent;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.util.CmdUtil;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * R3-2: Round3-2
 * Handle response message of command LOGIN_EXECU from server,
 * most important thing is to check the RETCODE whether is 0(success),
 *  
 * R1: AuthStartReq -> R2: AuthStartRsp -> R3: AuthAnswerReq -> R4: AuthAnswerRsp
 * 
 * @author bbstone
 *
 */
@Slf4j
public class AuthAnswerMessageHandler implements MessageHandler {

	
	public void handle(ChannelHandlerContext ctx, CmdRspEvent cmdRspEvent, ConnInfo connInfo) {
		int retCode = cmdRspEvent.getRetCode();
		String rspData = cmdRspEvent.getRetData();
		if (retCode == 0 && StringUtils.isNotBlank(rspData)) {
			AuthAnswerRsp rsp = JSON.parseObject(rspData, AuthAnswerRsp.class);
			
			// TODO check the cliRandAnswer to make sure the server is the right one(not the fake server)
			String cliRandAnswer = rsp.getCliRandAnswer();
			if ( checkCliRandAnswer(connInfo, cliRandAnswer)) {
				ClientContextHolder.getContext(connInfo.connId()).getClientAuthInfo().setCliRandAnswer(cliRandAnswer);
				
				// auth successfully
				String accessToken = rsp.getAccessToken();
				ClientContextHolder.getContext(connInfo.connId()).getClientAuthInfo().setAccessToken(accessToken);
				// remove password provided by user, TODO NOTICE, cannot remove password, reconnect need password
//				connInfo.setPassword(null);
				
				long timecost = cmdRspEvent.getRspTs() - cmdRspEvent.getReqTs();
				log.debug("auth answer time cost: {} ms", timecost);
				
				// do some post process after auth success
				ClientContextHolder.getClientProcessor(connInfo.connId()).processAuthSuccess(ctx, cmdRspEvent.getConnId());
			} else {
				log.error("fake server connected, procedure abort!");
//				clientContext.removeClientAuthInfo(connInfo.connId());
				// notify connect wait for complete threads
				ClientContextHolder.getClientConnector(connInfo.connId()).setStatus(ConnStatus.FAIL);
				ClientContextHolder.getClientConnector(connInfo.connId()).updateConnStatus(connInfo.connId(), ConnStatus.FAIL);
			}
		}
		
	}
	
	private boolean checkCliRandAnswer(ConnInfo connInfo, String cliRandAnswer) {
		if (StringUtils.isBlank(cliRandAnswer))
			return false;

		ClientAuthInfo authInfo = ClientContextHolder.getContext(connInfo.connId()).getClientAuthInfo();
		String cliRandAnswerCalc = CmdUtil.calcCliRandAnswer(authInfo.getCliRand(), connInfo.getPassword());

		return cliRandAnswer.equals(cliRandAnswerCalc);
	}
	

}
