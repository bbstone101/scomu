package com.bbstone.client.core;

import com.bbstone.client.core.model.CmdEvent;
import com.bbstone.comm.model.CmdResult;
import com.bbstone.comm.model.CmdRspEvent;

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
	public void dispatch(ChannelHandlerContext channelHandlerContext, ClientContext clientContext,
			CmdRspEvent cmdRspEvent) {
		MessageHandler handler = ClientContextHolder.getMessageHandlerRegister(clientContext.getConnId())
				.getHandler(cmdRspEvent.getCmd());
		if (handler != null) {
			// command handle by specified handler
			handler.handle(channelHandlerContext, clientContext, cmdRspEvent);
			// commands wich specified handler need to remove running cmd in pool in
			// concrete handlers

		} else { // command with no handler register, processing by following logic
//			CmdEvent cmdEvent = ClientContextHolder.getContext(cmdRspEvent.getConnId())
//					.getRunningCmd(cmdRspEvent.getId());
//			ClientContextHolder.getContext(cmdRspEvent.getConnId()).removeRunningCmd(cmdRspEvent.getId());

			CmdResult cmdResult = CmdResult.from(cmdRspEvent.getRetCode(), cmdRspEvent.getRetData(),
					cmdRspEvent.getRetData());

			log.debug("try to wakeup waiting requests ...");
			CmdEvent cmdEvent = ClientUtil.getAndRemoveRunningCmd(cmdRspEvent.getConnId(), cmdRspEvent.getId());
			log.info("removed cmd([cmd={}, cmdId={}, connId={}]) from running pool.", cmdRspEvent.getCmd(),
					cmdRspEvent.getId(), cmdRspEvent.getConnId());
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
	public void dispatchServerCmd(ChannelHandlerContext channelHandlerContext, ClientContext clientContext,
			CmdRspEvent cmdRspEvent) {
		MessageHandler handler = ClientContextHolder.getMessageHandlerRegister(clientContext.getConnId())
				.getHandler(cmdRspEvent.getCmd());
		if (handler == null) {
			log.error("not found handler for server command: {}", cmdRspEvent.getCmd());
			return;
		}

		// command handle by specified handler
		handler.handle(channelHandlerContext, clientContext, cmdRspEvent);

	}

}
