package com.bbstone.client.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.bbstone.client.core.ext.HeartBeatExecutor;
import com.bbstone.client.core.ext.SpeedChangeExecutor;
import com.bbstone.comm.model.ConnInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientContextHolder {

	private static AtomicInteger counter = new AtomicInteger();
	private static Map<String, ClientContext> clientContexts = new HashMap<>();
	private static Map<String, ClientConnector> clientConnectors = new HashMap<>();
	private static Map<String, MessageHandlerRegister> messageHandlerRegisters = new HashMap<>();
	private static Map<String, HeartBeatExecutor> heartBeatExecutors = new HashMap<>();
	private static Map<String, SpeedChangeExecutor> speedChangeExecutors = new HashMap<>();

	public static ClientContext newContext(ConnInfo connInfo) {
		if (clientContexts.containsKey(connInfo.connId())) {
			log.error("context with same connId({}) exists.", connInfo.connId());
			throw new RuntimeException("context with same connId exists, please check.");
		}
		// create context
		ClientContext clientContext = new ClientContext(connInfo);
		clientContexts.put(connInfo.connId(), clientContext);
		// return context
		return clientContext;
	}

	public static ClientConnector initConnector(String connId) {
		if (clientConnectors.containsKey(connId) || heartBeatExecutors.containsKey(connId)
				|| messageHandlerRegisters.containsKey(connId)) {
			log.error("connetor with same connId({}) exists.", connId);
			throw new RuntimeException("connetor with same connId exists, please check.");
		}
		ClientConnector clientConnector = new ClientConnector();
		clientConnectors.put(connId, clientConnector);
		int c = counter.incrementAndGet();
		log.info("current size of connectors: {}", c);
		// initial heart beat executor and message handler register
		heartBeatExecutors.put(connId, new HeartBeatExecutor());
		speedChangeExecutors.put(connId, new SpeedChangeExecutor());
		messageHandlerRegisters.put(connId, new MessageHandlerRegister());
		// return the new connector
		return clientConnector;
	}

	public static ClientContext selectContext() {
		if (clientContexts.isEmpty()) {
			throw new RuntimeException("open a client connection first.");
		}
		// TODO implement client load balancer algo
//		boolean selected = false;
		int runningCmdSize = 0;
		ClientContext selectedContext = null;
		for (Iterator<String> it = clientContexts.keySet().iterator(); it.hasNext();) {
			ClientContext context = clientContexts.get(it.next());
			if (context.isCmdAcceptable()) {
				// first time
				if (runningCmdSize == 0 && selectedContext == null) {
					runningCmdSize = context.runningCmdSize();
					selectedContext = context;
				}
				// current iterated context has less running cmds
				if (context.runningCmdSize() < runningCmdSize) {
					runningCmdSize = context.runningCmdSize();
					selectedContext = context;
				}
			}
//			if (selectedContext.getSocketChannel() != null && selectedContext.getSocketChannel().isActive()) {
//				selected = true;
//				return selectedContext;
//			}
		}
		if (selectedContext == null) {
			throw new RuntimeException("not found any availabe connection channel.");
		}
		return selectedContext;
	}

	public static int contextSize() {
		return clientContexts.size();
	}
	public static ClientContext getContext(String connId) {
		return clientContexts.get(connId);
	}

	public static ClientConnector getClientConnector(String connId) {
		return clientConnectors.get(connId);
	}

	public static MessageHandlerRegister getMessageHandlerRegister(String connId) {
		return messageHandlerRegisters.get(connId);
	}

	public static HeartBeatExecutor getHeartBeatExecutor(String connId) {
		return heartBeatExecutors.get(connId);
	}
	
	public static SpeedChangeExecutor getSpeedChangeExecutor(String connId) {
		return speedChangeExecutors.get(connId);
	}

	public static void removeContext(String connId) {
		if (clientContexts.get(connId) != null && !clientContexts.get(connId).isDestroyed()) {
			// reject cmd request and stop heat beat
			clientContexts.get(connId).rejectCmd();
			heartBeatExecutors.get(connId).disableHeartBeat();
			clientContexts.get(connId).destroy();

			// clear contexts
			clientContexts.remove(connId);
			clientConnectors.remove(connId);
			messageHandlerRegisters.remove(connId);
			heartBeatExecutors.remove(connId);
			int c = counter.decrementAndGet();
			log.info("current size of connectors: {}", c);
		}
	}

	public static int connectorSize() {
		return counter.get();
	}

}
