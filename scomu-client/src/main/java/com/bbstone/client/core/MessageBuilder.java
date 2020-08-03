package com.bbstone.client.core;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.bbstone.client.core.exception.AuthException;
import com.bbstone.comm.CmdConst;
import com.bbstone.comm.dto.CmdReqFactory;
import com.bbstone.comm.dto.req.AuthAnswerReq;
import com.bbstone.comm.dto.req.AuthStartReq;
import com.bbstone.comm.enums.CC;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.proto.CmdMsg;
import com.bbstone.comm.proto.CmdMsg.CmdReq;
import com.bbstone.comm.util.CmdUtil;
import com.bbstone.comm.util.TokenUtil;

public class MessageBuilder {
	
	public static CmdReq buildAuthStartReq(ConnInfo connInfo) {
		AuthStartReq authStartReq = CmdReqFactory.newAuthStartReq(connInfo.getUsername(), 
				CmdConst.WEB_API_VER, CmdConst.AUTH_TYPE_NORMAL, CmdConst.CRYPT_METHOD_NONE);
		ClientContextHolder.getContext(connInfo.connId()).getClientAuthInfo().setAuthStartReq(authStartReq);
		String data = JSON.toJSONString(authStartReq);
		CmdReq cmdReq = CmdMsg.CmdReq.newBuilder()
				.setId(TokenUtil.UUID32())
				.setCmd(CC.AUTH_START.name())
				.setData(data)
				.setConnId(connInfo.connId())
				.setCreateTs(System.currentTimeMillis())
				.setReqTs(System.currentTimeMillis())
//				.setAccessToken(null) // not assigned accessToken yet
				.build();
		return cmdReq;
	}
	
	/**
	 * auth answer request should not pass connInfo.password through network
	 * 
	 * the srvrandAnswer contains meaning of the password(only used for calculate hex),
	 * 
	 * server will respose a auth token for the rest request
	 * 
	 * @param connInfo
	 * @param srvrand
	 * @param clirand
	 * @return
	 */
	public static CmdReq buildAuthAnswerReq(ConnInfo connInfo, String srvRand, String cliRand) {
		// TODO password should not store plain text in connInfo instance for security protection
		String srvRandAnswer = CmdUtil.calcSrvRandAnswer(srvRand, connInfo.getPassword());
		
		AuthAnswerReq reqData = CmdReqFactory.newAuthAnswerReq(srvRandAnswer, cliRand);
		String data = JSON.toJSONString(reqData);
		CmdReq cmdReq = CmdMsg.CmdReq.newBuilder()
				.setId(TokenUtil.UUID32())
				.setCmd(CC.AUTH_ANSWER.name())
				.setData(data)
				.setConnId(connInfo.connId())
				.setCreateTs(System.currentTimeMillis())
				.setReqTs(System.currentTimeMillis())
//				.setAccessToken(null) // not assigned accessToken yet
				.build();
		return cmdReq;
	}
	
	public static CmdReq buildCmdReq(String connId, String cmd, String data) throws AuthException {
		String accessToken = getAccessToken(connId);
		if (StringUtils.isBlank(accessToken)) {
			throw new AuthException("not found accessToken.");
		};
		CmdReq cmdReq = CmdMsg.CmdReq.newBuilder()
				.setId(TokenUtil.UUID32())
				.setCmd(cmd)
				.setData(data)
				.setConnId(connId)
				.setCreateTs(System.currentTimeMillis())
				.setReqTs(System.currentTimeMillis())
				.setAccessToken(accessToken)
				.build();
		return cmdReq;
	}
	
	public static CmdReq buildCmdReq(CmdReqEvent cmdReqEvent) throws AuthException {
		String accessToken = getAccessToken(cmdReqEvent.getConnId());
		if (StringUtils.isBlank(accessToken)) {
			throw new AuthException("not found accessToken.");
		};
		CmdReq cmdReq = CmdMsg.CmdReq.newBuilder()
				.setId(StringUtils.isNotBlank(cmdReqEvent.getId()) ? cmdReqEvent.getId() : TokenUtil.UUID32())
				.setCmd(cmdReqEvent.getCmd())
				.setData(cmdReqEvent.getData())
				.setConnId(cmdReqEvent.getConnId())
				.setCreateTs(cmdReqEvent.getCreateTs())
				.setReqTs(System.currentTimeMillis())
				.setAccessToken(accessToken)
				.build();
		return cmdReq;
	}
	
	private static String getAccessToken(String connId) {
		String accessToken = ClientContextHolder.getContext(connId).getClientAuthInfo().getAccessToken();
//		if (StringUtils.isBlank(accessToken)) {
//			throw new RuntimeException("not found access token, please login first.");
//		}
		return accessToken;
	}

}
