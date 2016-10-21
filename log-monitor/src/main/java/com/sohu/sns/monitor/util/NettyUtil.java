package com.sohu.sns.monitor.util;

import com.sohu.sns.monitor.netty.ChannelFutureListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

public class NettyUtil {
	public static void writeResponse(HttpRequest httpRequest, StringBuilder content, Channel channel) {
 		ByteBuf buf = copiedBuffer(content.toString(), CharsetUtil.UTF_8);
		content.setLength(0); 
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8"); 
		channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		channel.close();
	}
}
