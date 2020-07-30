package com.bbstone.client.core.handler;

import com.bbstone.client.core.ConnectionManager;
import com.bbstone.comm.model.CmdRspEvent;
import com.bbstone.comm.model.ConnInfo;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StopReconnMessageHandler implements MessageHandler {

	@Override
	public void handle(ChannelHandlerContext ctx, CmdRspEvent cmdRspEvent, ConnInfo connInfo) {
//		Connection connection = ClientContextHolder.getClientConnection(cmdRspEvent.getConnId());
		ConnectionManager.close(connInfo.connId());
		log.info("stop client connection becasue heart beat timeout.");
	}

}
