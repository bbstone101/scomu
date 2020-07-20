package com.bbstone.client.core;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

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

	private volatile ConnStatus connStatus = null;
	private static final AtomicReferenceFieldUpdater<Connector, ConnStatus> RESULT_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(Connector.class, ConnStatus.class, "connStatus");

	public void connect(ConnInfo connInfo) {
		Connection bootstrap = new Connection(connInfo);
		bootstrap.startup();
		ClientContextHolder.getClientSession(connInfo.connId()).saveConnection(bootstrap);

		// connecting...
		connStatus = ConnStatus.CONNECTING;
		updateConnStatus(connInfo.connId(), ConnStatus.CONNECTING);

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

	public void disconnect(String connId) {
		ClientContextHolder.getClientSession(connId).getSocketChannel().closeFuture()
				.addListener(new ChannelFutureListener() {
					public void operationComplete(ChannelFuture f) throws Exception {
						if (f.isSuccess()) {
							log.info("channel is closed.");
						} else {
							log.info("channel close fail.");
						}
					}

				});
		ClientContextHolder.getClientSession(connId).getConnection().shutdown();
		// !import! all ref instance including threads derived by client main thread
		// should end, or client will not exit
		ClientContextHolder.getClientSession(connId).clearSession();
	}
	
	public void setStatus(ConnStatus status) {
		if (RESULT_UPDATER.compareAndSet(this, ConnStatus.CONNECTING, status)) {
			synchronized (this) {
				notifyAll();
			}
		}
	}

	public void updateConnStatus(String connId, ConnStatus status) {
		ClientContextHolder.getClientSession(connId).setConnStatus(status);
	}

	public ConnStatus getConnStatus(String connId) {
		return ClientContextHolder.getClientSession(connId).getConnStatus();
	}

}
