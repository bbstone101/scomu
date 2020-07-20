package com.bbstone.server.core;

import com.bbstone.comm.model.ConnInfo;

import io.netty.channel.ChannelHandlerContext;

/**
 * processor of client handler
 * 
 * 
 * 
 * @author bbstone
 *
 */
public class ServerProcessor {
	

	public static void processInitial() {
		
		// initial register command handlers
		ServerContext.saveMsgHandlerRegister(new MessageHandlerRegister());

	}
	
	
	
	/**
	 * when channel is active, do auth
	 * @param ctx
	 */
	public static void processActive(ChannelHandlerContext ctx, ConnInfo connInfo) {
		
	}
	
	public static void processInactive(ChannelHandlerContext ctx, ConnInfo connInfo) {
		// TODO update status
		
		
	}
	
	public static void processExceptionCaught(ChannelHandlerContext ctx, ConnInfo connInfo) {
		
	}
	

}
