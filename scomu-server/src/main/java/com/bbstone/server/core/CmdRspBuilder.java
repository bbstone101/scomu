package com.bbstone.server.core;

import com.alibaba.fastjson.JSON;
import com.bbstone.comm.dto.CmdRspFactory;
import com.bbstone.comm.dto.rsp.AuthAnswerRsp;
import com.bbstone.comm.dto.rsp.AuthStartRsp;
import com.bbstone.comm.enums.CC;
import com.bbstone.comm.enums.RetCode;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.proto.CmdMsg;
import com.bbstone.comm.proto.CmdMsg.CmdRsp;
import com.bbstone.comm.util.CmdUtil;

public class CmdRspBuilder {
	
	/**
	 * build CmdRsp with no body data
	 * @param cmdReqEvent
	 * @return
	 */
	public static CmdRsp buildRspWithNoBody(CmdReqEvent cmdReqEvent) {
		CmdRsp cmdRsp = CmdMsg.CmdRsp.newBuilder()
				.setId(cmdReqEvent.getId())
				.setCmd(cmdReqEvent.getCmd())
				.setRetCode(RetCode.SUCCESS.code())
				.setRetMsg(RetCode.SUCCESS.descp())
				.setRetData(CmdUtil.getEmptyRspData())
				.setReqTs(cmdReqEvent.getReqTs())
				.setRecvTs(System.currentTimeMillis())
				.setRspTs(System.currentTimeMillis())
				.setConnId(cmdReqEvent.getConnId())
				.build();
		return cmdRsp;
	}
	
	/**
	 * build CmdRsp with body data
	 * @param cmdReqEvent
	 * @param data
	 * @return
	 */
	public static CmdRsp buildRsp(CmdReqEvent cmdReqEvent, String data) {
		CmdRsp cmdRsp = CmdMsg.CmdRsp.newBuilder()
				.setId(cmdReqEvent.getId())
				.setCmd(cmdReqEvent.getCmd())
				.setRetCode(RetCode.SUCCESS.code())
				.setRetMsg(RetCode.SUCCESS.descp())
				.setRetData(data)
				.setReqTs(cmdReqEvent.getReqTs())
				.setRecvTs(System.currentTimeMillis())
				.setRspTs(System.currentTimeMillis())
				.setConnId(cmdReqEvent.getConnId())
				.build();
		return cmdRsp;
	}
	
	/**
	 * build heart beat CmdRsp
	 * @param cmdReqEvent
	 * @return
	 */
	public static CmdRsp buildHeartBeatRsp(CmdReqEvent cmdReqEvent) {
		CmdRsp cmdRsp = CmdMsg.CmdRsp.newBuilder()
				.setId(cmdReqEvent.getId())
				.setCmd(CC.HEART_BEAT.name())
				.setRetCode(RetCode.SUCCESS.code())
				.setRetMsg(RetCode.SUCCESS.descp())
				.setRetData(CmdUtil.getEmptyRspData())
				.setReqTs(cmdReqEvent.getReqTs())
				.setRecvTs(System.currentTimeMillis())
				.setRspTs(System.currentTimeMillis())
				.setConnId(cmdReqEvent.getConnId())
				.build();
		return cmdRsp;
	}
	
	/**
	 * build auth start CmdRsp
	 * @param cmdReqEvent
	 * @param srvRand
	 * @return
	 */
	public static CmdRsp buildAuthStartRsp(CmdReqEvent cmdReqEvent, String srvRand) {
		AuthStartRsp reqData = CmdRspFactory.newAuthStartRsp(srvRand);
		String data = JSON.toJSONString(reqData);
		CmdRsp cmdRsp = CmdMsg.CmdRsp.newBuilder()
				.setId(cmdReqEvent.getId())
				.setCmd(cmdReqEvent.getCmd())
				.setRetCode(RetCode.SUCCESS.code())
				.setRetMsg(RetCode.SUCCESS.descp())
				.setRetData(data)
				.setReqTs(cmdReqEvent.getReqTs())
				.setRecvTs(System.currentTimeMillis())
				.setRspTs(System.currentTimeMillis())
				.setConnId(cmdReqEvent.getConnId())
//				.setAccessToken(null) // not assigned accessToken yet
				.build();
		return cmdRsp;
	}
	
	/**
	 * build auth answer CmdRsp
	 * @param cmdReqEvent
	 * @param cliRand
	 * @param accessToken
	 * @param password
	 * @return
	 */
	public static CmdRsp buildAuthAnswerRsp(CmdReqEvent cmdReqEvent, String cliRand, String accessToken, String password) {
		// TODO password should not store plain text in connInfo instance for security protection
//		String password = ServerUtil.findPassword(username);
		String cliRandAnswer = CmdUtil.calcCliRandAnswer(cliRand, password);
		
		AuthAnswerRsp rspData = CmdRspFactory.newAuthAnswerRsp(cliRandAnswer, accessToken);
		String data = JSON.toJSONString(rspData);
		
		CmdRsp cmdRsp = CmdMsg.CmdRsp.newBuilder()
				.setId(cmdReqEvent.getId())
				.setCmd(cmdReqEvent.getCmd())
				.setRetCode(RetCode.SUCCESS.code())
				.setRetMsg(RetCode.SUCCESS.descp())
				.setRetData(data)
				.setReqTs(cmdReqEvent.getReqTs())
				.setRecvTs(System.currentTimeMillis())
				.setRspTs(System.currentTimeMillis())
				.setConnId(cmdReqEvent.getConnId())
				.setAccessToken(accessToken)
				.build();
		return cmdRsp;
	}
	
	/**
	 * build fail auth answer CmdRsp
	 * @param cmdReqEvent
	 * @return
	 */
	public static CmdRsp buildFailAuthAnswerRsp(CmdReqEvent cmdReqEvent) {
		
		AuthAnswerRsp rspData = CmdRspFactory.newAuthAnswerRsp(null, null);
		String data = JSON.toJSONString(rspData);
		
		CmdRsp cmdRsp = CmdMsg.CmdRsp.newBuilder()
				.setId(cmdReqEvent.getId())
				.setCmd(cmdReqEvent.getCmd())
				.setRetCode(RetCode.FAIL.code())
				.setRetMsg(RetCode.FAIL.descp())
				.setRetData(data)
				.setReqTs(cmdReqEvent.getReqTs())
				.setRecvTs(System.currentTimeMillis())
				.setRspTs(System.currentTimeMillis())
				.setConnId(cmdReqEvent.getConnId())
//				.setAccessToken(null) // not assigned accessToken yet
				.build();
		return cmdRsp;
	}
	
	

}
