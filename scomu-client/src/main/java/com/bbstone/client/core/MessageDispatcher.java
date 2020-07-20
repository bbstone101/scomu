package com.bbstone.client.core;

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

}
