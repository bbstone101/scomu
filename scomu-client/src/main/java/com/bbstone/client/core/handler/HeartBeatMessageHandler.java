package com.bbstone.client.core.handler;

import com.bbstone.comm.model.CmdRspEvent;
import com.bbstone.comm.model.ConnInfo;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatMessageHandler implements MessageHandler {

	@Override
	public void handle(ChannelHandlerContext ctx, CmdRspEvent cmdRspEvent, ConnInfo connInfo) {
		log.debug("heart beat response from server");
		// do nothing, only receive server heart beat response

	}

}
