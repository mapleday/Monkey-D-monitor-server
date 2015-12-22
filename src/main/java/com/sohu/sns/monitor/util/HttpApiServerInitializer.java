package com.sohu.sns.monitor.util;

import com.sohu.sns.monitor.netty.HttpObjectAggregator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class HttpApiServerInitializer extends ChannelInitializer<SocketChannel> {

	EventExecutorGroup executor;

	public static int EventExecutorThreadSize = 10;

	public HttpApiServerInitializer() {
		//初始化RequestProcessor
		executor= new DefaultEventExecutorGroup(EventExecutorThreadSize);
        RequestProcessor.init();
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline(); 
		pipeline.addLast("decoder", new HttpRequestDecoder()); 
		pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(10*1024*1024));
		pipeline.addLast(executor,new HttpApiServerHandler());
		//pipeline.addLast("handler", new HttpApiServerHandler());
	}
}
