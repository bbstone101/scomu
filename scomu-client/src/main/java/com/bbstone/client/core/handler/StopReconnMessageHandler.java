package com.bbstone.client.core.handler;

import com.bbstone.client.core.ClientConnectionManager;
import com.bbstone.client.core.ClientContext;
import com.bbstone.client.core.MessageHandler;
import com.bbstone.comm.model.CmdRspEvent;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StopReconnMessageHandler implements MessageHandler {

	@Override
	public void handle(ChannelHandlerContext channelHandlerContext, ClientContext clientContext,
			CmdRspEvent cmdRspEvent) {
		log.info("stop client connection becasue heart beat timeout.");
//		Connection connection = ClientContextHolder.getClientConnection(cmdRspEvent.getConnId());
		if (ClientConnectionManager.isOpen(clientContext.getConnId())) {
			ClientConnectionManager.close(clientContext.getConnId());
		}
	}

}
