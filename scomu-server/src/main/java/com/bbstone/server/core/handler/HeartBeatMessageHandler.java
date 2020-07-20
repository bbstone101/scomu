package com.bbstone.server.core.handler;

import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.proto.CmdMsg.CmdRsp;
import com.bbstone.server.core.CmdRspBuilder;
import com.bbstone.server.core.MessageHandler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartBeatMessageHandler implements MessageHandler {

	@Override
	public void handle(ChannelHandlerContext ctx, CmdReqEvent cmdReqEvent) {
		log.debug("heart beat req from client, cmdId: {}", cmdReqEvent.getId());
		CmdRsp cmdRsp = CmdRspBuilder.buildHeartBeatRsp(cmdReqEvent);
		ctx.channel().writeAndFlush(cmdRsp);
	}
	
	

}
