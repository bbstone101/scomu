package com.bbstone.server.core.base;

import com.alibaba.fastjson.JSON;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.proto.CmdMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerChannelHandler extends SimpleChannelInboundHandler<CmdMsg.CmdReq> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CmdMsg.CmdReq msg) throws Exception {
		// dispatch msg to message handler
		CmdReqEvent cmdReqEvent = CmdReqEvent.from(msg.getId(), 
									msg.getCmd(), 
									msg.getData(), 
									msg.getCreateTs(),
									msg.getReqTs(), 
									msg.getConnId());
		// print log for debug
		log.debug("server receive req msg: {}", JSON.toJSONString(cmdReqEvent));
		// dispatch request message to cmd's corresponding handler
		MessageDispatcher.dispatch(ctx, cmdReqEvent);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
		log.info("=========== new connection accepted  ===========");
		// TODO check connect ip whether in blacklist
		
		
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		log.info("=========== one connection close ===========");
		// TODO update server load(real-time number of connections)
		
		
	}

}
