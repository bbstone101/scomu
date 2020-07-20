package com.bbstone.client.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.bbstone.client.core.model.CmdEvent;
import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.client.core.model.Scheduler;
import com.bbstone.comm.model.ConnInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * handle client-server logic
 * 
 * 
 * 
 * @author bbstone
 *
 */
@Slf4j
public class ClientSession {
	

	// ------------------------------------------------------------------------------
	// CmdEvent LifeCycle ->
	// ------------------------
	// CmdEvent created -> enqueue(req) -> outqueue(req) -> send2mt5
	// -> mt5Rsp -> enqueue(rsp) -> outqueue(rsp) -> client code|end
	//
	// ------------------------------------------------------------------------------
	/** if CmdEvent not consume in 2000ms, request will timeout */
	public static int CMD_REQ_TIMEOUT = 2 * 1000; // 2s(2000ms)

	public static TimeUnit CMD_RSP_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;

	/** if CmdEvent not response data from mt5 in 5000ms, cmd will timeout */
	public static int CMD_RSP_TIMEOUT = 5 * 1000; // 5s(5000ms)
	/**
	 * the time of CommandConsumerTask will wait for next consumer CmdEvent if time
	 * over, CommandConsumerTask will end process, and reponse timeout to client
	 * code,
	 */
	public static int CMD_RSP_TIMEOUT_MIN = 4500; // 4.5s(4500ms)

	/** CommandConsumerTask idle period when there is a command processing, */
	public static int REQ_CONSUME_IDLE_TIME = 500; // ms

	/** actual timeout of the cmd */
	public static int REQ_CONSUME_IDLE_TIME_MAX = CMD_RSP_TIMEOUT + REQ_CONSUME_IDLE_TIME; // ms

	
	// --------------------------------------------------------------------------------------
	
	/** every running cmd will store here, cmd will be deleted in this case: 1) cmd rsp;2)timeout;3)crash(re-initial) */
	/** Running Cmd Pool, <cmdId, CmdEvent> */
	private ConcurrentMap<String, CmdEvent> runningReqCmdPool = new ConcurrentHashMap<>(); 
	
	private List<AuthSuccessListener> authSuccessListeners = new ArrayList<>();
	private List<Scheduler> authSuccessSchedulers = new ArrayList<>();
	
	
	private ConnInfo connInfo;
	private ConnStatus connStatus;
	private Connection connection;
	private SocketChannel socketChannel;
	private ChannelHandlerContext channelHandlerContext;



	ClientSession() {
		
	}
	
	public void clearSession() {
		connInfo = null;
		connStatus = null;
		connection  = null;
		socketChannel = null;
		channelHandlerContext = null;
		
		authSuccessListeners.clear();;
		authSuccessSchedulers.clear();;
		runningReqCmdPool.clear();;
		
		
		log.info("client session is cleared");
	}
	
	
	
	public ConnStatus getConnStatus() {
		return connStatus;
	}

	public void setConnStatus(ConnStatus connStatus) {
		this.connStatus = connStatus;
	}

	// ------- running cmd 
	public void addRunningCmd(CmdEvent cmdEvent) {
		runningReqCmdPool.put(cmdEvent.getCmdId(), cmdEvent);
	}
	
	public CmdEvent getRunningCmd(String cmdId) {
		return runningReqCmdPool.get(cmdId);
	}
	
	public void removeRunningCmd(String cmdId) {
		runningReqCmdPool.remove(cmdId);
	}
	

	public void saveConnInfo(ConnInfo connInfo) {
		this.connInfo = connInfo;
	}

	public ConnInfo getConnInfo() {
		return connInfo;
	}

	public void saveConnection(Connection client) {
		this.connection = client;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public void saveSocketChannel(SocketChannel channel) {
		this.socketChannel = channel;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	
	// channel handler context
	public ChannelHandlerContext getChannelHandlerCtx() {
		return channelHandlerContext;
	}

	public void updateChannelHandlerCtx(ChannelHandlerContext chCtx) {
		this.channelHandlerContext = chCtx;
	}

	public List<AuthSuccessListener> getAuthSuccessListeners() {
		return authSuccessListeners;
	}

	public void addAuthSuccessListener(AuthSuccessListener authSuccessListener) {
		authSuccessListeners.add(authSuccessListener);
	}

	public List<Scheduler> getAuthSucessSchedulers() {
		return authSuccessSchedulers;
	}

	public void addAuthSuccessScheduler(Scheduler scheduler) {
		authSuccessSchedulers.add(scheduler);
	}

	public void clearAllAuthSuccessScheduler() {
		authSuccessSchedulers.clear();
	}


}
