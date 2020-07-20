package com.bbstone.client.core;

import com.bbstone.comm.MessageFuture;

public class MessageFutureFactory {
	
	public static MessageFuture newInstance() {
		return new MessageFuture();
	}

}


