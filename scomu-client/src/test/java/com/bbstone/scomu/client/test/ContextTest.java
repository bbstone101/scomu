//package com.bbstone.scomu.client.test;
//
//import com.bbstone.client.core.ClientContext;
//import com.bbstone.client.core.ClientContextHolder;
//import com.bbstone.comm.util.ConnUtil;
//
//import lombok.extern.slf4j.Slf4j;
///**
// * put this class to core for test
// */
//@Slf4j
//public class ContextTest {
//
//	public static void main(String[] args) {
//		ClientContext cc = ClientContextHolder.newContext(ConnUtil.from("127.0.0.1", 8899, "demo", "demopass"));
//		MyHandler handler = new MyHandler(cc);
//		handler.handler();
//		
//		log.info("token: {}", cc.getClientAuthInfo().getAccessToken());
//	}
//
//}
//
//class MyHandler {
//
//	private ClientContext cc;
//	private Test1 test1 = new Test1();
//
//	MyHandler(ClientContext cc) {
//		this.cc = cc;
//	}
//
//	public void handler() {
//		test1.update(cc);
//	}
//}
//
//class Test1 {
//	public Test1() {
//	}
//
//	public void update(ClientContext cc) {
//		cc.getClientAuthInfo().setAccessToken("TEST0000000000000000001");
//	}
//}