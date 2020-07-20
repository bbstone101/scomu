package com.bbstone.comm.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class AuthAnswerRsp {

	private String cliRandAnswer;
	
	private String accessToken;
	
	
}
