package com.bbstone.client.core.handler;

import com.bbstone.client.core.ClientContext;
import com.bbstone.client.core.MessageHandler;
import com.bbstone.comm.model.CmdRspEvent;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatMessageHandler implements MessageHandler {

	@Override
	public void handle(ChannelHandlerContext channelHandlerContext, ClientContext clientContext,
			CmdRspEvent cmdRspEvent) {
		log.debug("heart beat response from server");
		// do nothing, only receive server heart beat response

	}

}
