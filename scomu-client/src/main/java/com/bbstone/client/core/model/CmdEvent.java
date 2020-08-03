package com.bbstone.client.core.model;

import com.bbstone.client.core.MessageFuture;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.model.CmdRspEvent;

import lombok.Data;

@Data
public class CmdEvent {

	// request/response command id
	private String cmdId;
	
	// request event
	private CmdReqEvent cmdReqEvent;
	
	// response event
	private CmdRspEvent cmdRspEvent;
	
	// request future
	private MessageFuture msgFuture;
	
}
