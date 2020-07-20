package com.bbstone.client.core;

import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.comm.ConfigConst;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.proto.CmdMsg.CmdReq;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * processor of client handler
 * 
 * 
 * 
 * @author bbstone
 *
 */
@Slf4j
public class ClientProcessor {

	public void processInitial(ConnInfo connInfo) {
		// statis
		ClientContextHolder.getContext(connInfo.connId()).rejectCmd();
	}

	/**
	 * when channel is active, do auth
	 * 
	 * @param ctx
	 */
	public void processActive(ChannelHandlerContext ctx, ConnInfo connInfo) {
		// R1-1: auth start
		CmdReq cmdReq = ClientContextHolder.getCmdReqBuilder(connInfo.connId()).buildAuthStartReq(connInfo);
		ctx.channel().writeAndFlush(cmdReq);
	}

	public void processInactive(ChannelHandlerContext ctx, ConnInfo connInfo) {
		ClientContextHolder.getContext(connInfo.connId()).rejectCmd();
	}
	
	public void processInactiveByClose(ChannelHandlerContext ctx, ConnInfo connInfo) {
		ClientContextHolder.getContext(connInfo.connId()).rejectCmd();
		ClientContextHolder.getClientSession(connInfo.connId()).clearSession();
		ClientContextHolder.getClientSession(connInfo.connId()).clearAllAuthSuccessScheduler();
		ClientContextHolder.getHeartBeatExecutor(connInfo.connId()).disableHeartBeat();
		// last destroy context
		ClientContextHolder.getContext(connInfo.connId()).destroy();
	}

	public void processExceptionCaught(ChannelHandlerContext ctx, ConnInfo connInfo) {
		// close client
		ClientContextHolder.getContext(connInfo.connId()).rejectCmd();
	}

	public void processAuthSuccess(ChannelHandlerContext ctx, String connId) {
		// connect/login success
		ClientContextHolder.getContext(connId).incConnTimes();

		// start to accept command
		ClientContextHolder.getContext(connId).acceptCmd();

		ClientContextHolder.getClientSession(connId).updateChannelHandlerCtx(ctx);
		// every 100s send a HEART_BEAT cmd to server to keep connection alived
		if (ConfigConst.ENABLED == ClientConfig.heartBeatEnabled) {
			ClientContextHolder.getHeartBeatExecutor(connId).enableHeartBeat();
			ClientContextHolder.getHeartBeatExecutor(connId).keepAlived(connId);
		}

		// notify connect wait for complete threads
		ClientContextHolder.getClientConnector(connId).setStatus(ConnStatus.CONNECTED);
		// update client session connection status
		ClientContextHolder.getClientConnector(connId).updateConnStatus(connId, ConnStatus.CONNECTED);

		log.debug("auth success, connection is ready for transmitting commands...");
		// execute authSuccessListeners
//		List<AuthSuccessListener> authSuccessListeners = ClientSession.getAuthSuccessListeners(connId);
//		log.info("notify auth success listeners(total: {}).", authSuccessListeners.size());
//		for (AuthSuccessListener listener : authSuccessListeners) {
//			listener.invoke(ctx);
//		}

		// execute authSucess Schedulers
//		List<Scheduler> schedulers = ClientSession.getAuthSucessSchedulers(connId);
//		log.info("notify auth success schedulers(total: {}).", schedulers.size());
//		for (Scheduler scheduler : schedulers) {
//			// TODO if re-connect will cause exception here ?
//			log.info("try to schedule one task at fixed rate ....");
//			scheduler.getScheduledExecutorService().scheduleAtFixedRate(scheduler.getCommand(), scheduler.getInitialDelay(), scheduler.getPeriod(), scheduler.getUnit());
//		}
	}

}
