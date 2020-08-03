package com.bbstone.client.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.bbstone.client.core.ext.AuthSuccessListener;
import com.bbstone.client.core.model.CmdEvent;
import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.client.core.model.Scheduler;
import com.bbstone.client.core.model.Statis;
import com.bbstone.comm.model.ClientAuthInfo;
import com.bbstone.comm.model.ConnInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

/**
 * 
 * handle client side context only,
 * 
 * if any associate with server side, should handle in ClientSession class
 * 
 * 
 * @author bbstone
 *
 */
public class ClientContext {

	private String connId;
	private ConnInfo connInfo;
	private ConnStatus connStatus;
	
	private Statis statis;
	private ClientAuthInfo clientAuthInfo;
	
	/** every running cmd will store here, cmd will be deleted in this case: 1) cmd rsp;2)timeout;3)crash(re-initial) */
	/** Running Cmd Pool, <cmdId, CmdEvent> */
	private ConcurrentMap<String, CmdEvent> runningReqCmdPool = new ConcurrentHashMap<>(); 
	
	private List<AuthSuccessListener> authSuccessListeners = new ArrayList<>();
	private List<Scheduler> authSuccessSchedulers = new ArrayList<>();
	
	private SocketChannel socketChannel;
	private ChannelHandlerContext channelHandlerContext;

	private boolean destoryed = false;
	
	private volatile boolean retry;
	private AtomicInteger timeoutReqs = new AtomicInteger();
	
	
	
	
	
	ClientContext(ConnInfo connInfo) {
		this.connInfo = connInfo;
		this.connId = connInfo.connId();
		initial(connId);
	}

	// --------- life cycle
	/**
	 * initial a new client context
	 * 
	 * @param connId
	 */
	void initial(String connId) {
		retry = true;
		statis = new Statis();
		clientAuthInfo = new ClientAuthInfo();
		
		destoryed = false;
	}

	/**
	 * destroy a client context
	 * 
	 * @param
	 */
	void destroy() {
		retry = false;
		statis = null;
		clientAuthInfo = null;
		
		connInfo = null;
		connStatus = null;
//		connection  = null;
		socketChannel = null;
		channelHandlerContext = null;
		
		authSuccessListeners.clear();
		authSuccessSchedulers.clear();
		runningReqCmdPool.clear();
		
		destoryed = true;
	}

	// --------------
	
	
	public boolean isDestroyed() {
		return destoryed;
	}
	
	public boolean isRetry() {
		return retry;
	}

	void setRetry(boolean retry) {
		this.retry = retry;
	}

	String connId() {
		return connId;
	}

	public int incrementAndGetTimeoutReqs() {
		return timeoutReqs.incrementAndGet();
	}
	
	public int getTimeoutReqSize() {
		return timeoutReqs.get();
	}
	
	ChannelHandlerContext getChannelHandlerContext() {
		return channelHandlerContext;
	}

	void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
		this.channelHandlerContext = channelHandlerContext;
	}

	ConcurrentMap<String, CmdEvent> getRunningReqCmdPool() {
		return runningReqCmdPool;
	}

	void setRunningReqCmdPool(ConcurrentMap<String, CmdEvent> runningReqCmdPool) {
		this.runningReqCmdPool = runningReqCmdPool;
	}

	List<Scheduler> getAuthSuccessSchedulers() {
		return authSuccessSchedulers;
	}

	void setAuthSuccessSchedulers(List<Scheduler> authSuccessSchedulers) {
		this.authSuccessSchedulers = authSuccessSchedulers;
	}

	void setConnId(String connId) {
		this.connId = connId;
	}

	void setConnInfo(ConnInfo connInfo) {
		this.connInfo = connInfo;
	}
	void setSocketChannel(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	void setStatis(Statis statis) {
		this.statis = statis;
	}

	void setClientAuthInfo(ClientAuthInfo clientAuthInfo) {
		this.clientAuthInfo = clientAuthInfo;
	}

	// ----------------------- statis
	private Statis getStatis() {
		return statis;
	}

	int connTimes() {
		return statis.getConnectTimes();
	}

	void incConnTimes() {
		statis.setConnectTimes(connTimes() + 1);
	}

	/** force start from 1 */
	int retryTimes() {
		int retryTimes = getStatis().getRetryTimes();
		return (retryTimes == 0) ? 1 : retryTimes;
	}

	void incRetryTimes() {
		statis.setRetryTimes(retryTimes() + 1);
	}

	void resetRetryTimes() {
		statis.setRetryTimes(0);
	}

	boolean isExceedRetryMax() {
		return ClientConfig.retryIntvl * retryTimes() > ClientConfig.retryMax;

	}

	// ------------ cmd accept
	public boolean isCmdAcceptable() {
		return statis.isCmdAcceptable();
	}

	void rejectCmd() {
		statis.setCmdAcceptable(false);
	}

	void acceptCmd() {
		statis.setCmdAcceptable(true);
	}

	void saveClientAuthInfo(ClientAuthInfo clientAuthInfo) {
		this.clientAuthInfo = clientAuthInfo;
	}

	ClientAuthInfo getClientAuthInfo() {
		return clientAuthInfo;
	}

	ConnStatus getConnStatus() {
		return connStatus;
	}

	void setConnStatus(ConnStatus connStatus) {
		this.connStatus = connStatus;
	}

	// ------- running cmd 
	void addRunningCmd(CmdEvent cmdEvent) {
		runningReqCmdPool.put(cmdEvent.getCmdId(), cmdEvent);
	}
	
	CmdEvent getRunningCmd(String cmdId) {
		return runningReqCmdPool.get(cmdId);
	}
	
	void removeRunningCmd(String cmdId) {
		runningReqCmdPool.remove(cmdId);
	}
	
	int runningCmdSize() {
		return runningReqCmdPool.size();
	}
	

	void saveConnInfo(ConnInfo connInfo) {
		this.connInfo = connInfo;
	}

	public ConnInfo getConnInfo() {
		return connInfo;
	}

	public String getConnId() {
		return connId;
	}

//	void saveConnection(ClientConnection client) {
//		this.connection = client;
//	}
//
//	ClientConnection getConnection() {
//		return this.connection;
//	}

	void saveSocketChannel(SocketChannel channel) {
		this.socketChannel = channel;
	}

	SocketChannel getSocketChannel() {
		return socketChannel;
	}


	List<AuthSuccessListener> getAuthSuccessListeners() {
		return authSuccessListeners;
	}

	public void addAuthSuccessListener(AuthSuccessListener authSuccessListener) {
		authSuccessListeners.add(authSuccessListener);
	}

	List<Scheduler> getAuthSucessSchedulers() {
		return authSuccessSchedulers;
	}

	void addAuthSuccessScheduler(Scheduler scheduler) {
		authSuccessSchedulers.add(scheduler);
	}

	void clearAllAuthSuccessScheduler() {
		authSuccessSchedulers.clear();
	}

	
	
	

}
