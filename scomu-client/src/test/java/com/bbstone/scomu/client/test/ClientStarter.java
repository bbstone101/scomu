package com.bbstone.scomu.client.test;

import com.bbstone.client.api.ClientAPI;
import com.bbstone.client.core.ConnectionManager;
import com.bbstone.comm.util.ConnUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientStarter {

	public static void main(String[] args) throws Exception {
		
		ConnectionManager.open(ConnUtil.from("127.0.0.1", 8899, "demo", "demopass"));
		ClientAPI.getOrder("1");

	}

}
