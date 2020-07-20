package com.bbstone.server.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bbstone.comm.enums.CC;
import com.bbstone.server.core.handler.AuthAnswerMessageHandler;
import com.bbstone.server.core.handler.AuthStartMessageHandler;
import com.bbstone.server.core.handler.DefaultMessageHandler;
import com.bbstone.server.core.handler.HeartBeatMessageHandler;


/**
 * 
 * Command Reponse Message Handler Register
 * 
 * only the registered command handler will handle corresponding command's response from server, 
 * 
 * if income a command but register not found any command handler, the command will be ignored.
 * 
 * @author bbstone
 *
 */
public class MessageHandlerRegister {
	
	/** <cmd, cmd_processor> */
	private Map<String, MessageHandler> cmdHandlers = new HashMap<>();
	
	public MessageHandlerRegister() {
		init();
	}
	
	/**
	 * register a command processor to the command
	 * 
	 * @param command
	 * @param cp
	 */
	public void register(String command, MessageHandler msgHandler) {
		cmdHandlers.put(command, msgHandler);
	}
	
	public MessageHandler getHandler(String command) {
		return cmdHandlers.get(command);
	}
	
	/**
	 * register default commands and their processor
	 */
	public void init() {
		// auth
		cmdHandlers.put(CC.AUTH_START.name(), new AuthStartMessageHandler());
		cmdHandlers.put(CC.AUTH_ANSWER.name(), new AuthAnswerMessageHandler());
		// hb
		cmdHandlers.put(CC.HEART_BEAT.name(), new HeartBeatMessageHandler());
		// test
		cmdHandlers.put(CC.GET_ORDER.name(), new DefaultMessageHandler());
		
		

	}
	
	public Set<String> commands() {
		return cmdHandlers.keySet();
	}
	
	
	

}
