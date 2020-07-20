package com.bbstone.client.core;

import io.netty.channel.ChannelHandlerContext;

public interface AuthSuccessListener {
	
	public void invoke(ChannelHandlerContext ctx);

}
