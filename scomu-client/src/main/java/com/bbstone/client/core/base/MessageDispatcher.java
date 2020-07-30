package com.bbstone.client.core.base;

import com.bbstone.client.core.ClientContextHolder;
import com.bbstone.client.core.handler.MessageHandler;
import com.bbstone.client.core.model.CmdEvent;
import com.bbstone.comm.model.CmdResult;
import com.bbstone.comm.model.CmdRspEvent;
import com.bbstone.comm.model.ConnInfo;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * all message will be routed by this dispatcher via command<->cmdHandler
 * 
 * @author bbstone
 *
 */
@Slf4j
public class MessageDispatcher {

	/**
	 * all commands request raise by client, and the response message handle here
	 * 
	 * @param ctx
	 * @param cmdRspEvent
	 * @param connInfo
	 */
	public void dispatch(ChannelHandlerContext ctx, CmdRspEvent cmdRspEvent, ConnInfo connInfo) {
		MessageHandler handler = ClientContextHolder.getMessageHandlerRegister(connInfo.connId())
				.getHandler(cmdRspEvent.getCmd());
		if (handler != null) {
			// command handle by specified handler
			handler.handle(ctx, cmdRspEvent, connInfo);
		} else { // command with no handler register, processing by following logic
			CmdEvent cmdEvent = ClientContextHolder.getClientSession(cmdRspEvent.getConnId())
					.getRunningCmd(cmdRspEvent.getId());
			ClientContextHolder.getClientSession(cmdRspEvent.getConnId()).removeRunningCmd(cmdRspEvent.getId());
			CmdResult cmdResult = CmdResult.from(cmdRspEvent.getRetCode(), cmdRspEvent.getRetData(),
					cmdRspEvent.getRetData());
			log.debug("try to wakeup waiting requests ...");
			cmdEvent.getMsgFuture().setResult(cmdResult);
		}
	}

	/**
	 * all command request raise by server, will handler here(with no connId in
	 * cmdRspEvent)
	 * 
	 * @param ctx
	 * @param cmdRspEvent
	 */
	public void dispatchServerCmd(ChannelHandlerContext ctx, CmdRspEvent cmdRspEvent, ConnInfo connInfo) {
		MessageHandler handler = ClientContextHolder.getMessageHandlerRegister(connInfo.connId())
				.getHandler(cmdRspEvent.getCmd());
		if (handler == null) {
			log.error("not found handler for server command: {}", cmdRspEvent.getCmd());
			return;
		}

		// command handle by specified handler
		handler.handle(ctx, cmdRspEvent, connInfo);

	}

}
