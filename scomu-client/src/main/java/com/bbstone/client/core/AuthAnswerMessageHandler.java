package com.bbstone.client.core;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.bbstone.client.core.ext.AuthSuccessListener;
import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.comm.ConfigConst;
import com.bbstone.comm.dto.rsp.AuthAnswerRsp;
import com.bbstone.comm.enums.RetCode;
import com.bbstone.comm.model.ClientAuthInfo;
import com.bbstone.comm.model.CmdRspEvent;
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

	
	public void handle(ChannelHandlerContext channelHandlerContext, ClientContext clientContext, CmdRspEvent cmdRspEvent) {
		int retCode = cmdRspEvent.getRetCode();
		String rspData = cmdRspEvent.getRetData();
		if (retCode == 0 && StringUtils.isNotBlank(rspData)) {
			AuthAnswerRsp rsp = JSON.parseObject(rspData, AuthAnswerRsp.class);
			
			// Tcheck the cliRandAnswer to make sure the server is the right one(not the fake server)
			String cliRandAnswer = rsp.getCliRandAnswer();
			if ( checkCliRandAnswer(clientContext, cliRandAnswer)) {
				clientContext.getClientAuthInfo().setCliRandAnswer(cliRandAnswer);
//				ClientContextHolder.getContext(clientContext.getConnId()).getClientAuthInfo().setCliRandAnswer(cliRandAnswer);
				
				// auth successfully
				String accessToken = rsp.getAccessToken();
				clientContext.getClientAuthInfo().setAccessToken(accessToken);
//				ClientContextHolder.getContext(clientContext.getConnId()).getClientAuthInfo().setAccessToken(accessToken);
				// remove password provided by user, TODO NOTICE, cannot remove password, reconnect need password
//				connInfo.setPassword(null);
				
				long timecost = cmdRspEvent.getRspTs() - cmdRspEvent.getReqTs();
				log.debug("auth answer time cost: {} ms", timecost);
				
				// do some post process after auth success
				processAuthSuccess(channelHandlerContext, clientContext);
			} else {
				log.error("fake server connected, procedure abort!");
				processAuthRejectByClient(channelHandlerContext, cmdRspEvent.getConnId());

			}
		} 
		// auth fail
		else if (RetCode.FAIL.code() == retCode) {
			log.error("auth check not passed.");
			processAuthRejectByServer(channelHandlerContext, cmdRspEvent.getConnId());
			
			
		}
		
	}
	
	private boolean checkCliRandAnswer(ClientContext clientContext, String cliRandAnswer) {
		if (StringUtils.isBlank(cliRandAnswer))
			return false;

		ClientAuthInfo authInfo = clientContext.getClientAuthInfo();
		String cliRandAnswerCalc = CmdUtil.calcCliRandAnswer(authInfo.getCliRand(), clientContext.getConnInfo().getPassword());

		return cliRandAnswer.equals(cliRandAnswerCalc);
	}
	
	public void processAuthSuccess(ChannelHandlerContext channelHandlerContext, ClientContext clientContext) {
		String connId = clientContext.getConnId();
		// connect/login success
		clientContext.incConnTimes();
//		ClientContextHolder.getContext(connId).incConnTimes();

		// start to accept command
		clientContext.acceptCmd();
		clientContext.setChannelHandlerContext(channelHandlerContext);
//		ClientContextHolder.getContext(connId).acceptCmd();
//		ClientContextHolder.getContext(connId).setChannelHandlerContext(channelHandlerContext);
		// every 100s send a HEART_BEAT cmd to server to keep connection alived
		if (ConfigConst.ENABLED == ClientConfig.heartBeatEnabled) {
			ClientContextHolder.getHeartBeatExecutor(connId).enableHeartBeat();
			ClientContextHolder.getHeartBeatExecutor(connId).keepAlived(connId);
		}
		
		if (ConfigConst.ENABLED == ClientConfig.speedCalcEnabled) {
			ClientContextHolder.getSpeedChangeExecutor(connId).enable();
			ClientContextHolder.getSpeedChangeExecutor(connId).start(connId);
		}

		// notify connect threads which waiting for complete
		ClientContextHolder.getClientConnector(connId).setStatus(clientContext, ConnStatus.CONNECTED);
		// update client session connection status
//		ClientContextHolder.getClientConnector(connId).updateConnStatus(clientContext, ConnStatus.CONNECTED);

		log.debug("auth success, connection is ready for transmitting commands...");
		// execute authSuccessListeners
		List<AuthSuccessListener> authSuccessListeners = ClientContextHolder.getContext(connId).getAuthSuccessListeners();
		log.info("notify auth success listeners(total: {}, connId: {}).", authSuccessListeners.size(), connId);
		for (AuthSuccessListener listener : authSuccessListeners) {
			listener.invoke(channelHandlerContext);
		}

		// execute authSucess Schedulers
//		List<Scheduler> schedulers = ClientSession.getAuthSucessSchedulers(connId);
//		log.info("notify auth success schedulers(total: {}).", schedulers.size());
//		for (Scheduler scheduler : schedulers) {
//			// TODO if re-connect will cause exception here ?
//			log.info("try to schedule one task at fixed rate ....");
//			scheduler.getScheduledExecutorService().scheduleAtFixedRate(scheduler.getCommand(), scheduler.getInitialDelay(), scheduler.getPeriod(), scheduler.getUnit());
//		}
	}

	/**
	 * client check server info not passed(connect to fake server)
	 * @param ctx
	 * @param connId
	 */
	public void processAuthRejectByClient(ChannelHandlerContext ctx, String connId) {
		// nodify auth req to release lock(wait)
		ClientContextHolder.getClientConnector(connId).setStatus(ClientContextHolder.getContext(connId), ConnStatus.FAKE_SERVER);
//		ClientContextHolder.getClientConnector(connId).updateConnStatus(ClientContextHolder.getContext(connId), ConnStatus.AUTH_FAIL);

	}

	/**
	 * server check client connection info not passed
	 * @param ctx
	 * @param connId
	 */
	public void processAuthRejectByServer(ChannelHandlerContext ctx, String connId) {
//		clientContext.removeClientAuthInfo(clientContext.getConnId());
		// notify connect wait for complete threads
		ClientContextHolder.getClientConnector(connId).setStatus(ClientContextHolder.getContext(connId), ConnStatus.AUTH_FAIL);
//		ClientContextHolder.getClientConnector(connId).updateConnStatus(ClientContextHolder.getContext(connId), ConnStatus.AUTH_FAIL);
		// close connection
		ClientConnectionManager.close(connId);
	}


}
