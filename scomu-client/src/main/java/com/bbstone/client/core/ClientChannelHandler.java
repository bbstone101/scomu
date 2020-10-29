package com.bbstone.client.core;

import org.apache.commons.lang3.StringUtils;

import com.bbstone.comm.model.CmdRspEvent;
import com.bbstone.comm.proto.CmdMsg;
import com.bbstone.comm.proto.CmdMsg.CmdReq;

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

	private volatile boolean stop = false;
	private MessageDispatcher messageDispatcher = new MessageDispatcher();

	private ClientContext clientContext;

	public ClientChannelHandler(ClientContext clientContext) {
		this.clientContext = clientContext;
	}

	/**
	 * when channel is active, do auth
	 * 
	 * @param ctx
	 */
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("channel is actvie....");

		// ------- auth
		log.info("try to login server");
		clientContext.setChannelHandlerContext(ctx);
//		ClientContextHolder.getContext(clientContext.getConnId()).setChannelHandlerContext(ctx);
		// R1-1: auth start
		CmdReq cmdReq = MessageBuilder.buildAuthStartReq(clientContext.getConnInfo());
		ctx.writeAndFlush(cmdReq);
//		clientContext.getChannelHandlerContext().channel().writeAndFlush(cmdReq);
//		processActive();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CmdMsg.CmdRsp msg) throws Exception {
		if (stop)
			return;
		CmdRspEvent cmdRspEvent = CmdRspEvent.from(msg.getId(), msg.getCmd(), msg.getRetCode(), msg.getRetMsg(),
				msg.getRetData(), msg.getReqTs(), msg.getRecvTs(), msg.getRspTs(), msg.getConnId(),
				msg.getAccessToken());

		// ----------- dispatch server response message which raise request by server
		// check whether commands start with "SRV_CMD_", these commands has no connId,
		// raise request by server, like broadcast by server
		String cmd = msg.getCmd();
		if (cmd.startsWith("SRV_CMD_")) {
			messageDispatcher.dispatchServerCmd(ctx, clientContext, cmdRspEvent);
			return;
		}

		// ----------- dispatch server response message which raise request by client
		String connId = msg.getConnId();
		if (StringUtils.isBlank(connId)) {
			log.error("CmdRsp response message with blank connId.");
			return;
		}
		ClientContextHolder.getContext(connId).setTotalInBytes(msg.toByteArray().length);
		// dispatch msg to message handler
		messageDispatcher.dispatch(ctx, clientContext, cmdRspEvent);
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		log.info("client channel unregistered......");
		stop = true;
		// close client
		clientContext.rejectCmd();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}

	/**
	 * when channel is active, do auth
	 * 
	 * @param ctx
	 */
//	private void processActive() {
//		// R1-1: auth start
//		CmdReq cmdReq = CmdReqBuilder.buildAuthStartReq(clientContext.getConnInfo());
//		clientContext.getChannelHandlerContext().channel().writeAndFlush(cmdReq);
//	}

//	public void processExceptionCaught(ChannelHandlerContext ctx, ConnInfo connInfo) {
//		// close client
//		ClientContextHolder.getContext(connInfo.connId()).rejectCmd();
//	}

}
