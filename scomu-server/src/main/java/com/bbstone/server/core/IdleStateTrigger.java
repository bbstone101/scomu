package com.bbstone.server.core;

import io.netty.channel.ChannelHandler.Sharable;

import com.bbstone.comm.enums.CC;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.proto.CmdMsg.CmdRsp;
import com.bbstone.comm.util.TokenUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@Sharable
public class IdleStateTrigger extends ChannelInboundHandlerAdapter {

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent)evt).state();
			if (state == IdleState.READER_IDLE) {
				CmdReqEvent cmdReqEvent = CmdReqEvent.from(TokenUtil.UUID32(), CC.SRV_CMD_STOP_RECONN.name());
				// send msg to client, make it stop re-connect
				CmdRsp cmdRsp = CmdRspBuilder.buildRspWithNoBody(cmdReqEvent);
				ctx.channel().writeAndFlush(cmdRsp);
				
				// close client connection when heart-beat timeout
				ctx.channel().close();
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

}
