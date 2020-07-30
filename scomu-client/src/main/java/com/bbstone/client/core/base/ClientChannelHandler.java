package com.bbstone.client.core.base;

import org.apache.commons.lang3.StringUtils;

import com.bbstone.client.core.ClientContextHolder;
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
public class ClientChannelHandler extends SimpleChannelInboundHandler<CmdMsg.CmdRsp> {

	private ConnInfo connInfo;

	public ClientChannelHandler(ConnInfo connInfo) {
		this.connInfo = connInfo;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ClientContextHolder.getClientProcessor(connInfo.connId()).processActive(ctx, connInfo);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CmdMsg.CmdRsp msg) throws Exception {
		CmdRspEvent cmdRspEvent = CmdRspEvent.from(msg.getId(), msg.getCmd(), msg.getRetCode(), msg.getRetMsg(), msg.getRetData(),
				msg.getReqTs(), msg.getRecvTs(), msg.getRspTs(), msg.getConnId(), msg.getAccessToken());
		
		// ----------- dispatch server response message which raise request by server
		// check whether commands start with "SRV_CMD_", these commands has no connId, 
		// raise request by server, like broadcast by server
		String cmd = msg.getCmd();
		if (cmd.startsWith("SRV_CMD_")) {
			ClientContextHolder.getMessageDispatcher(connInfo.connId()).dispatchServerCmd(ctx, cmdRspEvent, connInfo);
			return;
		}
		
		// ----------- dispatch server response message which raise request by client
		String connId = msg.getConnId();
		if (StringUtils.isBlank(connId)) {
			log.error("CmdRsp response message with blank connId.");
			return ;
		}
		// dispatch msg to message handler  
		ClientContextHolder.getMessageDispatcher(msg.getConnId()).dispatch(ctx, cmdRspEvent, connInfo);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		log.info("client channel unregistered......");
		
	}
	
	

}
