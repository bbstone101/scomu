package com.bbstone.server.core.handler;

import com.alibaba.fastjson.JSON;
import com.bbstone.comm.dto.rsp.OrderRspDTO;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.proto.CmdMsg.CmdRsp;
import com.bbstone.comm.util.TokenUtil;
import com.bbstone.server.core.CmdRspBuilder;
import com.bbstone.server.core.MessageHandler;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMessageHandler implements MessageHandler  {

	@Override
	public void handle(ChannelHandlerContext ctx, CmdReqEvent cmdReqEvent) {
		log.debug("get order by id, cmdId: {}", cmdReqEvent.getId());
		
		OrderRspDTO order = new OrderRspDTO();
		order.setId(TokenUtil.UUID32());
		order.setSymbol("EURUSD");
		String data = JSON.toJSONString(order);
		
		CmdRsp cmdRsp = CmdRspBuilder.buildRsp(cmdReqEvent, data);
		ctx.channel().writeAndFlush(cmdRsp);
		
	}

}
