package com.bbstone.client.core.handler;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.bbstone.client.core.ClientContextHolder;
import com.bbstone.client.util.ClientUtil;
import com.bbstone.comm.dto.rsp.AuthStartRsp;
import com.bbstone.comm.model.CmdRspEvent;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.proto.CmdMsg.CmdReq;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


/**
 * R1-2: Round1-2, handle auth_start response from server
 * 
 * Handler response message of command AUTH_START
 * 
 * R1: AuthStartReq -> R2: AuthStartRsp -> R3: AuthAnswerReq -> R4: AuthAnswerRsp
 * 
 * @author bbstone
 *
 */
@Slf4j
public class AuthStartMessageHandler implements MessageHandler {

	
	public void handle(ChannelHandlerContext ctx, CmdRspEvent cmdRspEvent, ConnInfo connInfo) {
		int retCode = cmdRspEvent.getRetCode();
		String rspData = cmdRspEvent.getRetData();
		if (retCode == 0 && StringUtils.isNotBlank(rspData)) {
			AuthStartRsp asr = JSON.parseObject(rspData, AuthStartRsp.class);
			
			String cliRand = ClientUtil.genClientRand();
			ClientContextHolder.getContext(connInfo.connId()).getClientAuthInfo().setCliRand(cliRand);
			
			// R3-1: send authAnswer
			CmdReq cmdReq = ClientContextHolder.getCmdReqBuilder(connInfo.connId()).buildAuthAnswerReq(connInfo, asr.getSrvRand(), cliRand);
			ctx.channel().writeAndFlush(cmdReq);
		} else if (retCode != 0) {
			log.debug("authStart cmd error. errcode: {}, errmsg: {}", cmdRspEvent.getRetCode(), cmdRspEvent.getRetMsg());
			
			
		} else {
			log.error("unknown server response: {}", JSON.toJSONString(cmdRspEvent));
			
		}
		
	}
	
	

}
