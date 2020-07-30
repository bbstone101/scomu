package com.bbstone.server.core.base;

import org.apache.commons.lang3.StringUtils;

import com.bbstone.comm.model.CmdReqEvent;
import com.bbstone.comm.model.ServerAuthInfo;
import com.bbstone.server.core.MessageHandler;
import com.bbstone.server.core.ServerContext;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * all message will be routed by this dispatcher via command<->cmdHandler
 * 
 * @author bbstone
 *
 */
@Slf4j
public class MessageDispatcher {

	public static void dispatch(ChannelHandlerContext ctx, CmdReqEvent cmdReqEvent) {
		// check auth (whether login)
		if ( freeAccess(cmdReqEvent.getCmd()) || authCheck(cmdReqEvent.getConnId()) ) {
			MessageHandler handler = ServerContext.getMsgHandlerRegister().getHandler(cmdReqEvent.getCmd());
			handler.handle(ctx, cmdReqEvent);
		} else {
			log.error("not auth access, please login first.");
			// TODO do some clean work here ??
		}
		
	}
	
	private static boolean authCheck(String connId) {
		ServerAuthInfo authInfo = ServerContext.getServerAuthInfo(connId);
		if (authInfo == null || StringUtils.isBlank(authInfo.getAccessToken())) {
			return false;
		}
		return true;
	}
	
	private static boolean freeAccess(String cmd) {
		if (StringUtils.isBlank(cmd)) return false;
		return ServerContext.freeAccessCmds().contains(cmd);
	}

}
