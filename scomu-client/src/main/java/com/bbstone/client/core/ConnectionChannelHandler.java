package com.bbstone.client.core;

import org.apache.commons.lang3.StringUtils;

import com.bbstone.comm.model.CmdRspEvent;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.proto.CmdMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * 
 * 
 * 
 * 
 * @author bbstone
 *
 */
@Slf4j
public class ConnectionChannelHandler extends SimpleChannelInboundHandler<CmdMsg.CmdRsp> {

	private ConnInfo connInfo;

	public ConnectionChannelHandler(ConnInfo connInfo) {
		this.connInfo = connInfo;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ClientContextHolder.getClientProcessor(connInfo.connId()).processActive(ctx, connInfo);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CmdMsg.CmdRsp msg) throws Exception {
		String connId = msg.getConnId();
		if (StringUtils.isBlank(connId)) {
			log.error("CmdRsp response message with blank connId.");
			return ;
		}
		// dispatch msg to message handler  
		CmdRspEvent cmdRspEvent = CmdRspEvent.from(msg.getId(), msg.getCmd(), msg.getRetCode(), msg.getRetMsg(), msg.getRetData(),
				msg.getReqTs(), msg.getRecvTs(), msg.getRspTs(), msg.getConnId(), msg.getAccessToken());
		ClientContextHolder.getMessageDispatcher(msg.getConnId()).dispatch(ctx, cmdRspEvent, connInfo);
	}

}
