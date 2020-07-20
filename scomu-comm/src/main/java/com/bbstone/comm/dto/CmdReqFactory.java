package com.bbstone.comm.dto;

import com.bbstone.comm.dto.req.AuthAnswerReq;
import com.bbstone.comm.dto.req.AuthStartReq;

public class CmdReqFactory {
	
	public static AuthStartReq newAuthStartReq(String username, String apiVersion, String authType, String cryptMethod) {
		return new AuthStartReq(username, apiVersion, authType, cryptMethod);
	}
	
	
	public static AuthAnswerReq newAuthAnswerReq(String srvRandAnswer, String cliRand) {
		return new AuthAnswerReq(srvRandAnswer, cliRand);
	}
	

}
