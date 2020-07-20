package com.bbstone.comm.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class AuthStartReq {

	private String username;

	private String apiVersion;

	/** M-manager, N-normal, T-test */
	private String authType;

	/** NONE — 无加密，AES256OFB — AES */
	private String cryptMethod;

}
