package com.bbstone.scomu.client.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.bbstone.client.core.ClientConnectionManager;
import com.bbstone.comm.model.ConnInfo;
import com.bbstone.comm.util.ConnUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(value=1)
public class ClientRunner implements ApplicationRunner {


	public void run(ApplicationArguments args) throws Exception {
//		ConnectionManager.open(new ConnInfo[] {
//				ConnUtil.from("127.0.0.1", 8898, "demo1", "demopass1"),
//				ConnUtil.from("127.0.0.1", 8899, "demo2", "demopass2")
//		});
		
	}

}
