package com.bbstone.client.core;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.bbstone.client.core.model.ConnStatus;

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
public class ClientConnector {

	private ClientConnection clientConnection;

	private String connId = null;
	// require for RESULT_UPDATER
	private volatile ConnStatus connStatus = null;
	private static final AtomicReferenceFieldUpdater<ClientConnector, ConnStatus> RESULT_UPDATER = AtomicReferenceFieldUpdater
			.newUpdater(ClientConnector.class, ConnStatus.class, "connStatus");

	/**
	 * call connect() method will create a new connection every time
	 * 
	 * @param connInfo
	 */
	public void connect(ClientContext clientContext) {
		this.connId = clientContext.connId();

		clientConnection = new ClientConnection();
		clientConnection.startup(clientContext);

		// connecting...
		connStatus = ConnStatus.CONNECTING;
		updateConnStatus(clientContext, ConnStatus.CONNECTING);

		log.info("client connecting ...");
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
				log.error("thread interrupted when wait for connection complete ", e);
			}
		}
	}

	public void disconnect() {
		ClientContext clientContext = ClientContextHolder.getContext(connId);
		log.info("step1/4: prepareing to close client connection -> reject comd req");
		clientContext.rejectCmd();
		log.info("step2/4: prepareing to close client connection -> disable reconnect try");
		log.info("------retry: {}", clientContext.isRetry());
		clientContext.setRetry(false);
		log.info("------retry: {}", clientContext.isRetry());
		log.info("step3/4: closing channel...");
		// close channel
		ChannelFuture closeFuture = clientContext.getSocketChannel().close();
		closeFuture.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
					log.info("channel is closed.");
				} else {
					log.info("channel close fail.");
				}
			}

		});
		log.info("step4/4: shuting down client(connection)...");
//		ClientContextHolder.getClientSession(this.connId).getSocketChannel().close().syncUninterruptibly();
		// shutdown EventLoopGroup(workerGroup)
		clientConnection.shutdown(clientContext);
//		ClientContextHolder.getClientSession(connId).getConnection().shutdown();
		// !import! all ref instance including threads derived by client main thread
		// should end, or client will not exit
//		ClientContextHolder.getClientSession(connId).clearSession();
	}

	public void setStatus(ClientContext clientContext, ConnStatus status) {
		if (RESULT_UPDATER.compareAndSet(this, ConnStatus.CONNECTING, status)) {
			updateConnStatus(clientContext, this.connStatus);
			synchronized (this) {
				notifyAll();
			}
		}
	}

	private void updateConnStatus(ClientContext clientContext, ConnStatus status) {
		clientContext.setConnStatus(status);
		if (ConnStatus.CONNECTED == status) {
			log.info("auth check passed.");
		} else if (ConnStatus.AUTH_FAIL == status || ConnStatus.FAKE_SERVER == status) {
//			this.disconnect(connId);
			boolean connected = ClientConnectionManager.isOpen(connId);
			log.info("check if client connected: {} ", connected);
			if (connected) {
				ClientConnectionManager.close(connId);
				log.info("disconnect by auth fail.");
			} else {
				log.info("client is not connected.");
//				this.disconnect();
//				ClientContextHolder.getContext(connId).destroy();
			}
			log.error("auth check fail, please make sure your connection information is correct.");
		}
	}

	public ConnStatus getConnStatus(ClientContext clientContext) {
		return clientContext.getConnStatus();
	}

}
