package com.bbstone.client.core;

import com.alibaba.fastjson.JSON;
import com.bbstone.client.core.exception.AuthException;
import com.bbstone.client.core.model.CmdEvent;
import com.bbstone.client.core.model.CmdEventFactory;
import com.bbstone.comm.enums.RetCode;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.model.CmdResult;
import com.bbstone.comm.proto.CmdMsg.CmdReq;
import com.bbstone.comm.util.TokenUtil;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientUtil {

//	private static ClientContext clientContext = null;
//
//	public static void setClientContext(ClientContext clientContext) {
//		ClientUtil.clientContext = clientContext;
//	}

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
		return (System.currentTimeMillis() - cmdReqEvent.getCreateTs() >= ClientConfig.reqTimeout);
	}

	/**
	 * CommandConsumerTask idle max total timeout, more than CmdRspTimeout(5s) one
	 * idle time (default 500ms), so default is 5.5s
	 * 
	 * @param cmdEvent
	 * @return
	 */
//	public static boolean isCmdTimeout(CmdReqEvent cmdReqEvent) {
//		if (cmdReqEvent == null || cmdReqEvent.getCreateTs() == 0) {
//			log.error("cmdEvent is null or createTime not set.");
//			return true;
//		}
//		return (System.currentTimeMillis() - cmdReqEvent.getCreateTs() >= ClientSession.REQ_CONSUME_IDLE_TIME_MAX);
//	}

	public static void sendReq(ChannelHandlerContext ctx, CmdReq cmdReq) {
		if (ctx == null || cmdReq == null) {
			log.error("command request not sent.");
			return;
		}
		ctx.writeAndFlush(cmdReq);
	}

	// ------------------------------ req process

	/**
	 * used ChannelHandlerContext to write, msg flow from next handler(write: next
	 * -> head, read: next -> tail), but if used SocketChannel, will start from
	 * tail(write: tail -> head, read: head -> tail),
	 * 
	 * @param cmdReqEvent
	 * @throws AuthException
	 */
	public static CmdResult sendReq(CmdReqEvent cmdReqEvent) {
//		SocketChannel sc = ClientSession.getSocketChannel(connId);
		ChannelHandlerContext ctx = ClientContextHolder.getContext(cmdReqEvent.getConnId()).getChannelHandlerContext();

		CmdReq cmdReq = null;
		CmdEvent cmdEvent = CmdEventFactory.newInstance();
		try {
			// build cmdReq
			cmdReq = MessageBuilder.buildCmdReq(cmdReqEvent);
			// save running cmd
			cmdEvent.setCmdId(cmdReqEvent.getId());
			cmdEvent.setCmdReqEvent(cmdReqEvent);
			cmdEvent.setMsgFuture(MessageFutureFactory.newInstance());
			ClientContextHolder.getContext(cmdReqEvent.getConnId()).addRunningCmd(cmdEvent);
			log.info("added cmd([cmd={}, cmdId={}, connId={}]) to running pool.", cmdReqEvent.getCmd(),
					cmdReqEvent.getId(), cmdReqEvent.getConnId());
//			log.info("added cmdEvent: {}", JSON.toJSONString(cmdEvent));
			// send req
//				sc.writeAndFlush(cmdReq);
//				ctx.writeAndFlush(cmdReq);
			sendReq(ctx, cmdReq);
		} catch (AuthException e1) {
			log.error("send command request error.", e1);
			return CmdResult.from(RetCode.FAIL.code(), RetCode.FAIL.descp());
		}
		CmdResult cmdResult = cmdEvent.getMsgFuture().getResult();
		int timeoutReqs = 0;
		if (RetCode.REQ_TIMEOUT.code() == cmdResult.code) {
			timeoutReqs = ClientContextHolder.getContext(cmdReqEvent.getConnId()).incrementAndGetTimeoutReqs();
		} else {
			timeoutReqs = ClientContextHolder.getContext(cmdReqEvent.getConnId()).getTimeoutReqSize();
		}
		log.info("**************>>>>>>>> recv get order result(timeout-size:{}): {}", timeoutReqs, JSON.toJSONString(cmdResult));
		return cmdResult;
	}

	/**
	 * only send request to server, and not wait for response (HeartBeat),
	 * 
	 * and this request command will not store to running command pool.
	 * 
	 * NOTICE: the command must be registered to the MessageHandlerRegister, or will
	 * cause exception
	 * 
	 * @param cmdReqEvent
	 * @return
	 * @throws AuthException
	 */
	public static void sendReqOnly(CmdReqEvent cmdReqEvent) {
		ChannelHandlerContext ctx = ClientContextHolder.getContext(cmdReqEvent.getConnId()).getChannelHandlerContext();
		CmdReq cmdReq;
		try {
			cmdReq = MessageBuilder.buildCmdReq(cmdReqEvent);
//				ctx.writeAndFlush(cmdReq);
			sendReq(ctx, cmdReq);
		} catch (AuthException e) {
			log.error("send command request error.", e);
		}

	}

	public static CmdEvent getAndRemoveRunningCmd(String connId, String cmdId) {
		CmdEvent cmdEvent = ClientContextHolder.getContext(connId).getRunningCmd(cmdId);
		ClientContextHolder.getContext(connId).removeRunningCmd(cmdId);
		return cmdEvent;
	}

}
