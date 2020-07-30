package com.bbstone.client.core;

import com.bbstone.client.core.base.ClientConfig;
import com.bbstone.client.core.base.Connector;
import com.bbstone.client.core.base.MessageDispatcher;
import com.bbstone.client.core.exception.AuthException;
import com.bbstone.client.core.model.CmdEvent;
import com.bbstone.client.core.model.CmdEventFactory;
import com.bbstone.client.core.model.Statis;
import com.bbstone.client.util.ClientUtil;
import com.bbstone.comm.enums.RetCode;
import com.bbstone.comm.model.ClientAuthInfo;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.model.CmdResult;
import com.bbstone.comm.proto.CmdMsg.CmdReq;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * handle client side context only,
 * 
 * if any associate with server side, should handle in ClientSession class
 * 
 * 
 * @author bbstone
 *
 */
@Slf4j
public class ClientContext {

	private static boolean TRUE = true;
	private static boolean FALSE = false;

	private String connId;
	private Connector clientConnector;
	private ClientProcessor clientProcessor;
	private ClientSession clientSession;
	private CmdReqBuilder cmdReqBuilder;
	private MessageDispatcher messageDispatcher;
	private MessageHandlerRegister messageHandlerRegister;
	private HeartBeatExecutor heartBeatExecutor;

	private Statis statis;
	private ClientAuthInfo clientAuthInfo;
	
//	private List<AuthSuccessListener> authSuccessListeners = new ArrayList<>();

	ClientContext(String connId) {
		this.connId = connId;
		initial(connId);
	}

	// --------- life cycle
	/**
	 * initial a new client context
	 * 
	 * @param connId
	 */
	void initial(String connId) {
		clientConnector = new Connector();
		clientProcessor = new ClientProcessor();
		clientSession = new ClientSession();
		cmdReqBuilder = new CmdReqBuilder();
		messageDispatcher = new MessageDispatcher();
		messageHandlerRegister = new MessageHandlerRegister();
		heartBeatExecutor = new HeartBeatExecutor();

		statis = new Statis();
		clientAuthInfo = new ClientAuthInfo();
	}

	/**
	 * destroy a client context
	 * 
	 * @param
	 */
	void destroy() {
		clientProcessor = null;
		clientSession = null;
		cmdReqBuilder = null;
		messageDispatcher = null;
		messageHandlerRegister = null;
		heartBeatExecutor = null;

		statis = null;
		clientAuthInfo = null;
	}

	// --------------

	public String connId() {
		return connId;
	}

	public Connector getClientConnector() {
		return clientConnector;
	}

	public ClientProcessor getClientProcessor() {
		return this.clientProcessor;
	}

	public ClientSession getClientSession() {
		return clientSession;
	}

	public CmdReqBuilder getCmdReqBuilder() {
		return cmdReqBuilder;
	}

	public MessageDispatcher getMessageDispatcher() {
		return messageDispatcher;
	}

	public MessageHandlerRegister getMessageHandlerRegister() {
		return messageHandlerRegister;
	}

	public HeartBeatExecutor getHeartBeatExecutor() {
		return heartBeatExecutor;
	}

	// ----------------------- statis
	private Statis getStatis() {
		return statis;
	}

	public int connTimes() {
		return statis.getConnectTimes();
	}

	public void incConnTimes() {
		statis.setConnectTimes(connTimes() + 1);
	}

	/** force start from 1 */
	public int retryTimes() {
		int retryTimes = getStatis().getRetryTimes();
		return (retryTimes == 0) ? 1 : retryTimes;
	}

	public void incRetryTimes() {
		statis.setRetryTimes(retryTimes() + 1);
	}

	public void resetRetryTimes() {
		statis.setRetryTimes(0);
	}

	public boolean isExceedRetryMax() {
		return ClientConfig.retryIntvl * retryTimes() > ClientConfig.retryMax;

	}

	// ------------ cmd accept
	public boolean isCmdAcceptable() {
		return statis.isCmdAcceptable();
	}

	public void rejectCmd() {
		statis.setCmdAcceptable(FALSE);
	}

	public void acceptCmd() {
		statis.setCmdAcceptable(TRUE);
	}

	public void saveClientAuthInfo(ClientAuthInfo clientAuthInfo) {
		this.clientAuthInfo = clientAuthInfo;
	}

	public ClientAuthInfo getClientAuthInfo() {
		return clientAuthInfo;
	}
	
	
//	public List<AuthSuccessListener> getAuthSuccessListeners() {
//		return this.authSuccessListeners;
//	}
//	
//	public void addAuthSuccessListener(AuthSuccessListener listener) {
//		this.authSuccessListeners.add(listener);
//	}
	
	// ------------------------------ req process

	/**
	 * used ChannelHandlerContext to write, msg flow from next handler(write: next
	 * -> head, read: next -> tail), but if used SocketChannel, will start from
	 * tail(write: tail -> head, read: head -> tail),
	 * 
	 * @param cmdReqEvent
	 * @throws AuthException 
	 */
	public CmdResult sendReq(CmdReqEvent cmdReqEvent) {

//		SocketChannel sc = ClientSession.getSocketChannel(connId);
		ChannelHandlerContext ctx = clientSession.getChannelHandlerCtx();

		CmdReq cmdReq = null;
		try {
			cmdReq = cmdReqBuilder.buildCmdReq(cmdReqEvent);
//			sc.writeAndFlush(cmdReq);
//			ctx.writeAndFlush(cmdReq);
			ClientUtil.sendReq(ctx, cmdReq);
		} catch (AuthException e1) {
			log.error("send command request error.", e1);
			return CmdResult.from(RetCode.FAIL.code(), RetCode.FAIL.descp());
		}

		CmdEvent cmdEvent = CmdEventFactory.newInstance();
		cmdEvent.setCmdId(cmdReqEvent.getId());
		cmdEvent.setCmdReqEvent(cmdReqEvent);
		cmdEvent.setMsgFuture(MessageFutureFactory.newInstance());
		clientSession.addRunningCmd(cmdEvent);
//		return cmdEvent.getMsgFuture();

		CmdResult cmdResult = null;
		try {
			cmdResult = cmdEvent.getMsgFuture().getResult();
			log.info("recv get order result.......");
		} catch (InterruptedException e) {
			return CmdResult.from(RetCode.THREAD_INTERRUPTED.code(), RetCode.THREAD_INTERRUPTED.descp());
		}
		return cmdResult;
	}
	
	/**
	 * only send request to server, and not wait for response (HeartBeat),
	 * 
	 * and this request command will not store to running command pool.
	 * 
	 * NOTICE: the command must be registered to the MessageHandlerRegister, or will cause exception
	 * 
	 * @param cmdReqEvent
	 * @return
	 * @throws AuthException 
	 */
	public void sendReqOnly(CmdReqEvent cmdReqEvent) {
		ChannelHandlerContext ctx = clientSession.getChannelHandlerCtx();
		CmdReq cmdReq;
		try {
			cmdReq = cmdReqBuilder.buildCmdReq(cmdReqEvent);
//			ctx.writeAndFlush(cmdReq);
			ClientUtil.sendReq(ctx, cmdReq);
		} catch (AuthException e) {
			log.error("send command request error.", e);
		}
		
	}

}
