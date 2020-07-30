package com.bbstone.client.core.base;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.bbstone.client.core.ClientContextHolder;
import com.bbstone.client.core.ConnectionManager;
import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.comm.model.ConnInfo;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * connect method wait for CONNECTED/FAIL result
 *
 * @author bbstone
 *
 */
@Slf4j
public class Connector {
	private String connId = null;
	private volatile ConnStatus connStatus = null;
	private static final AtomicReferenceFieldUpdater<Connector, ConnStatus> RESULT_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(Connector.class, ConnStatus.class, "connStatus");

	/**
	 * call connect() method will create a new connection every time
	 * @param connInfo
	 */
	public void connect(ConnInfo connInfo) {
		this.connId = connInfo.connId();
		
		Client bootstrap = new Client(connInfo);
		bootstrap.startup();
		ClientContextHolder.getClientSession(connInfo.connId()).saveConnection(bootstrap);

		// connecting...
		connStatus = ConnStatus.CONNECTING;
		updateConnStatus(ConnStatus.CONNECTING);

		log.info("client connecting ...");
		synchronized (this) {
			while (connStatus == ConnStatus.CONNECTING) {
				try {
					wait();
				} catch (InterruptedException e) {
					log.error("thread interrupted when wait for connection complete ", e);
				}
			}
		}
	}

	public void disconnect() {
		// close channel
		ChannelFuture closeFuture = ClientContextHolder.getClientSession(this.connId).getSocketChannel().close();
		closeFuture.addListener(new ChannelFutureListener() {
					public void operationComplete(ChannelFuture f) throws Exception {
						if (f.isSuccess()) {
							log.info("channel is closed.");
						} else {
							log.info("channel close fail.");
						}
					}

				});
//		ClientContextHolder.getClientSession(this.connId).getSocketChannel().close().syncUninterruptibly();
		// shutdown EventLoopGroup(workerGroup)
		ClientContextHolder.getClientSession(connId).getConnection().shutdown();
		// !import! all ref instance including threads derived by client main thread
		// should end, or client will not exit
//		ClientContextHolder.getClientSession(connId).clearSession();
	}
	
	public void setStatus(ConnStatus status) {
		if (RESULT_UPDATER.compareAndSet(this, ConnStatus.CONNECTING, status)) {
			synchronized (this) {
				notifyAll();
			}
		}
	}

	public void updateConnStatus(ConnStatus status) {
		ClientContextHolder.getClientSession(this.connId).setConnStatus(status);
		if (ConnStatus.CONNECTED == status) {
			log.info("auth check passed.");
		} else if (ConnStatus.AUTH_FAIL == status || ConnStatus.FAKE_SERVER == status) {
//			this.disconnect(connId);
			boolean connected = ConnectionManager.isOpen(connId);
			log.info("check if client connected: {} ", connected);
			if (connected) {
				ConnectionManager.close(connId);
				log.info("disconnect by auth fail.");
			} else {
				log.info("client is not connected.");
//				this.disconnect();
				
//				ClientContextHolder.getClientSession(connId).getConnection().setNoRetry(true);
//				ClientContextHolder.getClientSession(connId).clearSession();
				
//				ClientContextHolder.getContext(connId).destroy();
//				ClientContextHolder.getClientSession(connId).getConnection().shutdown();
			}
			log.error("auth check fail, please make sure your connection information is correct.");
		}
	}

	public ConnStatus getConnStatus() {
		return ClientContextHolder.getClientSession(this.connId).getConnStatus();
	}

}
