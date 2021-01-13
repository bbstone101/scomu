package com.bbstone.client.core;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.comm.proto.CmdMsg;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author bbstone
 *
 */
@Slf4j
public class ClientConnection {

	// reconnect is scheduled
	private volatile boolean retryScheduled = false;
	private volatile long lastRetryTime = 0L;

	private Bootstrap bootstrap = new Bootstrap();
	private EventLoopGroup workerGroup = new NioEventLoopGroup();


	public ClientConnection() {
	}

	public void startup(ClientContext clientContext) {
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.remoteAddress(clientContext.getConnInfo().getHost(), clientContext.getConnInfo().getPort());
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				ChannelPipeline pipeline = socketChannel.pipeline();

				// channel inactive handler
				pipeline.addFirst(new ChannelInboundHandlerAdapter() {
					public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//						super.channelInactive(ctx);
						log.debug("------retry: {}", clientContext.isRetry());
						log.info("channel inactive(catched by first handler), connId: {}", clientContext.getConnId());
						processInactive(clientContext, clientContext.isRetry());
					}
				});

				// ----------- register handler to pipeline
				// 解码器，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
				pipeline.addLast(new ProtobufVarint32FrameDecoder());
				// 将接收到的二进制文件解码成具体的实例，这边接收到的是服务端的CmdRsp对象实列
				pipeline.addLast(new ProtobufDecoder(CmdMsg.CmdRsp.getDefaultInstance()));
				// Google Protocol Buffers编码器
				pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
				// Google Protocol Buffers编码器
				pipeline.addLast(new ProtobufEncoder());

				// biz handler
				pipeline.addLast(new ClientChannelHandler(clientContext));
			}
		});

		doConnect(clientContext);
	}

	public void doConnect(ClientContext clientContext) {
		log.debug("------retry: {}", clientContext.isRetry());
		if (!clientContext.isRetry()) {
			log.info("retry is disabled, will not do connect to server");
			return;
		}
		// has been scheduled next retry, and now time is in last retry interval, skip
		if (retryScheduled && inLastRetryInterval(clientContext)) {
			log.info("exist retry schedule and now in the last retry interval, not connection now");
			return;
		}
		ChannelFuture future = bootstrap.connect(
				new InetSocketAddress(clientContext.getConnInfo().getHost(), clientContext.getConnInfo().getPort()));
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
					log.info("client connection established.");
					// ------- save socket channel and update connection status
					SocketChannel sc = (SocketChannel) f.channel();
					clientContext.saveSocketChannel(sc);
					clientContext.setConnStatus(ConnStatus.CONNECTED);
					// ------ reset re-connect variables
					log.debug("reset re-connect variables");
					retryScheduled = false;
					lastRetryTime = 0l;
					clientContext.resetRetryTimes();
					clientContext.incConnTimes();
					// remove scheduled connection
//					scheduledConnections.remove(clientContext.connId());

				} else {
					// connect fail, schedule next try
					scheduleNextTry(clientContext, f.channel(), clientContext.getConnId());
				}
			}
		});
	}

	/**
	 * shutdown event executors
	 */
	public void shutdown(ClientContext clientContext) {
//		retry = false;
		clientContext.setRetry(false);
		log.info("client shutdown proceeding...");
		// shut down executor
		if (!workerGroup.isShutdown()) {
			workerGroup.shutdownGracefully();
		}
		// terminate all tasks
		if (!workerGroup.isTerminated()) {
			// both can be close by client api and auth fail
			// handler(AuthAnswerMessageHandler)
			Future<?> future = workerGroup.terminationFuture();
			future.addListener(new FutureListener<Object>() {
				@Override
				public void operationComplete(Future<Object> future) throws Exception {
					if (future.isSuccess()) {
						// last destroy context
						clientContext.setRetry(false);
						ClientContextHolder.removeContext(clientContext.getConnId());
						log.debug("****====== context removed =======****");
						log.debug("---------> remain connectors: {}", ClientContextHolder.connectorSize());
					}
				}
			});
			// auth fail cannot close client connection, but can close via client api
//			workerGroup.terminationFuture().syncUninterruptibly();
		}
		log.info("event executor is shutdown.");

		try {
			// ensure that shutdown has actually completed and won't
			// cause class loader error if JVM starts unloading classes
			Thread.sleep(2);
		} catch (InterruptedException ignore) {
			// ignore
		}
		log.info("client shutdown success. connId: {}", clientContext.getConnId());
	}

	private boolean inLastRetryInterval(ClientContext clientContext) {
		int totalIntvl = clientContext.retryTimes() * ClientConfig.retryIntvl;
		long nowIntvl = System.currentTimeMillis() - lastRetryTime;
		log.info("will try to re-connect after {} ms.", nowIntvl);
		return nowIntvl < totalIntvl;
	}

	private void scheduleNextTry(ClientContext clientContext, Channel channel, String connId) {
		if (!clientContext.isRetry()) {
			log.warn("retry disabled, will not add next schedule retry");
			return;
		}
		if (ClientContextHolder.getContext(connId).isExceedRetryMax()) {
			log.info("retry exceed max retry times, client shutdown.");
			return;
		}
		log.info("clint connection lost, schedule next retry.");

		int x = (int) Math.pow(ClientConfig.retryMulti, ClientContextHolder.getContext(connId).retryTimes() - 1);
		int delay = ClientConfig.retryIntvl * x;

		log.debug("retryTimes: {}, delay: {}", ClientContextHolder.getContext(connId).retryTimes(), delay);
		channel.eventLoop().schedule(() -> doConnect(clientContext), delay, TimeUnit.SECONDS);
		clientContext.incRetryTimes();
		retryScheduled = true;
		lastRetryTime = System.currentTimeMillis();
	}

	private void processInactive(ClientContext clientContext, boolean retry) {
		clientContext.rejectCmd();
		if (retry) {
			log.info("processing channel inactive with connection retry. connId: {}", clientContext.getConnId());
			scheduleNextTry(clientContext, clientContext.getChannelHandlerContext().channel(),
					clientContext.getConnId());
		} else {
			log.info("processing channel inactive with noRetry. connId: {}", clientContext.getConnId());
			ClientContextHolder.getHeartBeatExecutor(clientContext.getConnId()).disableHeartBeat();
			// TODO clear here or in shutdown method?
			clientContext.clearAllAuthSuccessScheduler();
		}
	}


}
