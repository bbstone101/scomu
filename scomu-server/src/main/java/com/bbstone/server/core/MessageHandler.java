package com.bbstone.server.core;

import com.bbstone.comm.model.CmdReqEvent;

import io.netty.channel.ChannelHandlerContext;

/**
 * 
 * Base MessageHandler(including command & data handler) template, 
 * 
 * the sub-handler(sub-class) must provide cmdHandler instance and dataHandler instance,
 * 
 * if the command response no data can simply provide NoDataHandler
 * 
 * @author bbstone
 *
 */
public interface MessageHandler {
	
	public void handle(ChannelHandlerContext ctx, CmdReqEvent cmdReqEvent); 

}
