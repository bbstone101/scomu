package com.bbstone.comm.dto;

import com.bbstone.comm.dto.rsp.AuthAnswerRsp;
import com.bbstone.comm.dto.rsp.AuthStartRsp;

public class CmdRspFactory {
	
	public static AuthStartRsp newAuthStartRsp(String srvRand) {
		return new AuthStartRsp(srvRand);
	}
	
	public static AuthAnswerRsp newAuthAnswerRsp(String cliRandAnswer, String accessToken) {
		return new AuthAnswerRsp(cliRandAnswer, accessToken);
	}
	
	

}
