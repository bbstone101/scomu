package com.bbstone.server.core.handler;

import com.alibaba.fastjson.JSON;
import com.bbstone.comm.dto.req.AuthAnswerReq;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.proto.CmdMsg.CmdRsp;
import com.bbstone.server.core.CmdRspBuilder;
import com.bbstone.server.core.MessageHandler;
import com.bbstone.server.core.ServerContext;
import com.bbstone.server.core.ServerSession;
import com.bbstone.server.util.ServerUtil;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * R4-1: Round4-1
 * 
 * Handle reponse message of command LOGIN_EXECU from server,
 * most important thing is to check the RETCODE whether is 0(success),
 *  
 * R1: AuthStartReq -> R2: AuthStartRsp -> R3: AuthAnswerReq -> R4: AuthAnswerRsp
 * 
 * @author bbstone
 *
 */
@Slf4j
public class AuthAnswerMessageHandler implements MessageHandler {

	
	public void handle(ChannelHandlerContext ctx, CmdReqEvent cmdReqEvent) {
		String reqData = cmdReqEvent.getData();
		AuthAnswerReq req = JSON.parseObject(reqData, AuthAnswerReq.class);
		
		String cliRand = req.getCliRand();
		String srvRandAnswer = req.getSrvRandAnswer();
		// auth pass
		if ( ServerUtil.checkSrvRandAnswer(cmdReqEvent.getConnId(), srvRandAnswer) ) {
			log.info("auth check passed.");
			ServerContext.getServerAuthInfo(cmdReqEvent.getConnId()).setSrvRandAnswer(srvRandAnswer);
			
			// generate accessToken for user(client/connection)
			String accessToken = ServerUtil.genAccessToken();
			// TODO save accessToken to redis for cluster mode
			ServerContext.getServerAuthInfo(cmdReqEvent.getConnId()).setAccessToken(accessToken);
			
			String username = ServerContext.getServerAuthInfo(cmdReqEvent.getConnId()).getAuthStartReq().getUsername();
			String password = ServerUtil.findPassword(username);
			CmdRsp cmdRsp = CmdRspBuilder.buildAuthAnswerRsp(cmdReqEvent, cliRand, accessToken, password);
			ctx.channel().writeAndFlush(cmdRsp);
		} 
		// auth fail
		else {
			log.info("auth fail");
			CmdRsp cmdRsp = CmdRspBuilder.buildFailAuthAnswerRsp(cmdReqEvent);
			ctx.channel().writeAndFlush(cmdRsp);
			ServerContext.removeServerAuthInfo(cmdReqEvent.getConnId());
		}
		log.info("response auth_answer");
	}
	
	

}
