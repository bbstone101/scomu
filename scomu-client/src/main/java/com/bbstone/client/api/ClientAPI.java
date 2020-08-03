package com.bbstone.client.api;

import com.alibaba.fastjson.JSON;
import com.bbstone.client.core.ClientContextHolder;
import com.bbstone.client.core.ClientUtil;
import com.bbstone.comm.dto.req.OrderReqDTO;
import com.bbstone.comm.enums.CC;
import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.model.CmdResult;
import com.bbstone.comm.util.TokenUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * API )
 * 
 * @author bbstone
 *
 */
@Slf4j
public class ClientAPI {
	
	
	public static CmdResult getOrder(String orderId) {
		log.info("start get_order req...");
		OrderReqDTO order = new OrderReqDTO();
		order.setId(orderId);
		
		String data = JSON.toJSONString(order);
		String connId = ClientContextHolder.selectContext().getConnId();
		String cmdId = TokenUtil.UUID32();
		CmdReqEvent cmdReqEvent = CmdReqEvent.from(cmdId, CC.GET_ORDER.name(), 
				data, 
				System.currentTimeMillis(), 
				System.currentTimeMillis(), 
				connId);
		CmdResult cmdResult = ClientUtil.sendReq(cmdReqEvent);
		log.info("send GET_ORDER request to server");
		return cmdResult;
	}

}
