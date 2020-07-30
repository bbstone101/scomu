package com.bbstone.client.core.base;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.bbstone.client.core.ClientContextHolder;
import com.bbstone.client.core.model.ConnStatus;
import com.bbstone.comm.model.ConnInfo;
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
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author bbstone
 *
 */
@Slf4j
public class Client {

	private Bootstrap bootstrap = new Bootstrap();
	private EventLoopGroup workerGroup = new NioEventLoopGroup();

	private volatile boolean noRetry = false;
	private volatile boolean retryScheduled = false;
	private volatile long lastRetryTime = 0L;

	private ConnInfo connInfo;

	public Client(ConnInfo connInfo) {
		this.connInfo = connInfo;
		ClientContextHolder.newContext(connInfo.connId());
		ClientContextHolder.getClientSession(connInfo.connId()).saveConnection(this);
		ClientContextHolder.getClientProcessor(connInfo.connId()).processInitial(connInfo);
	}

	public void startup() {
		bootstrap.group(workerGroup);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.remoteAddress(connInfo.getHost(), connInfo.getPort());
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			protected void initChannel(SocketChannel socketChannel) throws Exception {
				ChannelPipeline pipeline = socketChannel.pipeline();
				// reconnect to server when channel inactive
				pipeline.addFirst(new ChannelInboundHandlerAdapter() {
					public void channelInactive(ChannelHandlerContext ctx) throws Exception {
						log.info("channel inactive(catched by first handler)........");
						if (noRetry) {
							log.info("user force close connection.");
							ClientContextHolder.getClientProcessor(connInfo.connId()).processInactiveByClose(ctx,
									connInfo);
						} else {
							super.channelInactive(ctx);
							ClientContextHolder.getClientProcessor(connInfo.connId()).processInactive(ctx, connInfo);
							scheduleNextTry(ctx.channel(), connInfo.connId());
						}
					}
				});

				// ----------- register handler to pipeline
				// 解码器，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
				pipeline.addLast(new ProtobufVarint32FrameDecoder());
				// 将接收到的二进制文件解码成具体的实例，这边接收到的是服务端的ResponseBank对象实列
				pipeline.addLast(new ProtobufDecoder(CmdMsg.CmdRsp.getDefaultInstance()));
				// Google Protocol Buffers编码器
				pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
				// Google Protocol Buffers编码器
				pipeline.addLast(new ProtobufEncoder());
//				pipeline.addLast(new ProtoClientHandler());
				pipeline.addLast(new ClientChannelHandler(connInfo));
			}
		});

		doConnect();
	}

	private boolean isLastRetryInterval(String connId) {
		int totalIntvl = ClientContextHolder.getContext(connInfo.connId()).retryTimes() * ClientConfig.retryIntvl;
		long nowIntvl = System.currentTimeMillis() - lastRetryTime;
		log.info("will try to re-connect after {} ms.", nowIntvl);
		return nowIntvl < totalIntvl;
	}

	public void doConnect() {
		// has been scheduled next retry, and now time is in last retry interval, skip
		if (retryScheduled && isLastRetryInterval(connInfo.connId()))
			return;
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(connInfo.getHost(), connInfo.getPort()));
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				if (f.isSuccess()) {
					SocketChannel sc = (SocketChannel) f.channel();
					// ------- save socket channel and update connection status
					ClientContextHolder.getClientSession(connInfo.getConnId()).saveSocketChannel(sc);
					ClientContextHolder.getClientSession(connInfo.getConnId()).setConnStatus(ConnStatus.CONNECTED);
					log.info("client connection established.");
					// ------ reset re-connect variables
					retryScheduled = false;
					lastRetryTime = 0l;
					ClientContextHolder.getContext(connInfo.getConnId()).resetRetryTimes();
					ClientContextHolder.getContext(connInfo.getConnId()).incConnTimes();
				} else {
					// connect fail, schedule next try
					scheduleNextTry(f.channel(), connInfo.connId());
				}
			}
		});
	}

	private void scheduleNextTry(Channel channel, String connId) {
		if (!ClientContextHolder.getContext(connId).isExceedRetryMax()) {
			log.info("clint connection lost, schedule next retry.");

			int x = (int) Math.pow(ClientConfig.retryMulti, ClientContextHolder.getContext(connId).retryTimes() - 1);
			int delay = ClientConfig.retryIntvl * x;

			log.debug("retryTimes: {}, delay: {}", ClientContextHolder.getContext(connId).retryTimes(), delay);
			channel.eventLoop().schedule(() -> doConnect(), delay, TimeUnit.SECONDS);
			ClientContextHolder.getContext(connId).incRetryTimes();
			retryScheduled = true;
			lastRetryTime = System.currentTimeMillis();
		} else {
			log.info("retry exceed max retry times, client shutdown.");
		}
	}

	/**
	 * shutdown event executors
	 */
	public void shutdown() {
		noRetry = true;
		log.info("client shutdown proceeding...");
		// shut down executor
		if (!workerGroup.isShutdown()) {
			workerGroup.shutdownGracefully();
		}
		// terminate all tasks
		if (!workerGroup.isTerminated()) {
			// both can be close by client api and auth fail handler(AuthAnswerMessageHandler)
			workerGroup.terminationFuture();
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
		log.info("client shutdown success.");
	}

	public void setNoRetry(boolean noRetry) {
		this.noRetry = noRetry;
	}

}
