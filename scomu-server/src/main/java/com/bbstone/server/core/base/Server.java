package com.bbstone.server.core.base;

import java.util.concurrent.TimeUnit;

import com.bbstone.comm.proto.CmdMsg;
import com.bbstone.comm.util.CipherUtil;
import com.bbstone.server.core.IdleStateTrigger;
import com.bbstone.server.core.ServerContext;
import com.bbstone.server.core.ServerProcessor;
import com.bbstone.server.core.model.ServerStatus;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {

	private EventLoopGroup bossGroup = new NioEventLoopGroup();
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	
	
	public Server() {
		// TODO register starting node to zk/master node
		ServerProcessor.processInitial();
	}
	
	/** bootstrap start */
	public void startup(String host, int port) {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap
			.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
//			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					ChannelPipeline pipeline = socketChannel.pipeline();
					// --- handle channel inactive
					pipeline.addFirst(new ChannelInboundHandlerAdapter() {
						public void channelInactive(ChannelHandlerContext ctx) throws Exception {
							super.channelInactive(ctx);
							log.info("channel inactive(catched by first handler)........");
							// TODO close channel if channel inactive
							ctx.channel().close();
							log.info("channel is closed.");
						}
					});
					// idle handler
					pipeline.addLast(new IdleStateHandler(ServerConfig.idleTimeout, 0, 0, TimeUnit.SECONDS));
//					IdleStateTrigger idleStateTrigger = new IdleStateTrigger();
					pipeline.addLast(new IdleStateTrigger());
					
					// --- register handler to pipeline
					// 解码器，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
					pipeline.addLast(new ProtobufVarint32FrameDecoder());
					// 服务器端接收的是客户端RequestUser对象，所以这边将接收对象进行解码生产实列
					pipeline.addLast(new ProtobufDecoder(CmdMsg.CmdReq.getDefaultInstance()));
					// Google Protocol Buffers编码器
					pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
					// Google Protocol Buffers编码器
					pipeline.addLast(new ProtobufEncoder());
					pipeline.addLast(new ServerChannelHandler());
				}
			});

		ChannelFuture channelFuture = serverBootstrap.bind(host, port);
		channelFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture f) throws Exception {
                if (f.isSuccess()) {
                	ServerContext.updateServerStatus(CipherUtil.md5(host+port), ServerStatus.STARTED);
                	log.info("server startup successfully!");
                } else {
                	log.info("server startup fail.");
                }
            }
        });
	}
	
	/**
	 * shutdown event executors
	 * 
	 */
	public void shutdown() {
		// Shut down all event loops to terminate all threads.
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		// Wait until all threads are terminated.
        bossGroup.terminationFuture().syncUninterruptibly();
        workerGroup.terminationFuture().syncUninterruptibly();
        
        try {
            // ensure that shutdown has actually completed and won't
            // cause class loader error if JVM starts unloading classes
            Thread.sleep(2);
        } catch (InterruptedException ignore) {
            // ignore
        }
	}
	
	
	
	

}
