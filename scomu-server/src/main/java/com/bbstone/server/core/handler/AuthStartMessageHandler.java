package com.bbstone.server.core.handler;

import com.alibaba.fastjson.JSON;
import com.bbstone.comm.dto.req.AuthStartReq;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.proto.CmdMsg.CmdRsp;
import com.bbstone.server.core.CmdRspBuilder;
import com.bbstone.server.core.MessageHandler;
import com.bbstone.server.core.ServerContext;
import com.bbstone.server.util.ServerUtil;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


/**
 * 
 * R2-1: Round2-1
 * 
 * Handler response message of command AUTH_START
 * 
 * 
 * R1: AuthStartReq -> R2: AuthStartRsp -> R3: AuthAnswerReq -> R4: AuthAnswerRsp
 * 
 * @author bbstone
 *
 */
@Slf4j
public class AuthStartMessageHandler implements MessageHandler {

	
	public void handle(ChannelHandlerContext ctx, CmdReqEvent cmdReqEvent) {
		String reqData = cmdReqEvent.getData();
		AuthStartReq authStartReq = JSON.parseObject(reqData, AuthStartReq.class);
		ServerContext.getServerAuthInfo(cmdReqEvent.getConnId()).setAuthStartReq(authStartReq);
		
		String srvRand = ServerUtil.genSrvRand();
		ServerContext.getServerAuthInfo(cmdReqEvent.getConnId()).setSrvRand(srvRand);
		
		CmdRsp cmdRsp = CmdRspBuilder.buildAuthStartRsp(cmdReqEvent, srvRand);
		ctx.channel().writeAndFlush(cmdRsp);
		log.info("response auth_start");
	}
	
	

}
