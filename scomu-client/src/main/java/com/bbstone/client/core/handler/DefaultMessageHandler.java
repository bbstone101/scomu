package com.bbstone.client.core.handler;

import com.bbstone.client.core.ClientContext;
import com.bbstone.client.core.ClientUtil;
import com.bbstone.client.core.MessageHandler;
import com.bbstone.client.core.model.CmdEvent;
import com.bbstone.comm.model.CmdResult;
import com.bbstone.comm.model.CmdRspEvent;

import io.netty.channel.ChannelHandlerContext;

public class DefaultMessageHandler implements MessageHandler {

	@Override
	public void handle(ChannelHandlerContext channelHandlerContext, ClientContext clientContext,
			CmdRspEvent cmdRspEvent) {
		CmdEvent cmdEvent = ClientUtil.getAndRemoveRunningCmd(cmdRspEvent.getConnId(), cmdRspEvent.getId());
		CmdResult cmdResult = CmdResult.from(cmdRspEvent.getRetCode(), cmdRspEvent.getRetData(), cmdRspEvent.getRetData());
		cmdEvent.getMsgFuture().setResult(cmdResult);
	}

}
