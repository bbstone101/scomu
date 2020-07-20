package com.bbstone.client.core.handler;

import com.bbstone.client.core.ClientContextHolder;
import com.bbstone.client.core.model.CmdEvent;
import com.bbstone.comm.model.CmdResult;
import com.bbstone.comm.model.CmdRspEvent;
import com.bbstone.comm.model.ConnInfo;

import io.netty.channel.ChannelHandlerContext;

public class DefaultMessageHandler implements MessageHandler  {

	@Override
	public void handle(ChannelHandlerContext ctx, CmdRspEvent cmdRspEvent, ConnInfo connInfo) {
		CmdEvent cmdEvent = ClientContextHolder.getClientSession(cmdRspEvent.getConnId()).getRunningCmd(cmdRspEvent.getId());
		ClientContextHolder.getClientSession(cmdRspEvent.getConnId()).removeRunningCmd(cmdRspEvent.getId());
		
		CmdResult cmdResult = CmdResult.from(cmdRspEvent.getRetCode(), cmdRspEvent.getRetData(), cmdRspEvent.getRetData());
		cmdEvent.getMsgFuture().setResult(cmdResult);
	}

}
