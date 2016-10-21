package com.sohu.sns.monitor.util;

import com.sohu.sns.monitor.constant.RequestValue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpApiServerHandler extends SimpleChannelInboundHandler<HttpObject> {
	private HttpRequest request;

	private final StringBuilder responseContent = new StringBuilder();
	private final StringBuilder path = new StringBuilder();
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //
	private HttpPostRequestDecoder postDecoder;
	private static final Logger LOG = LoggerFactory.getLogger(HttpApiServerHandler.class);

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (postDecoder != null) {
			postDecoder.cleanFiles();
		}
	}

	public void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
 		try {
			InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			Map<String, RequestValue> mapParams = new HashMap<String, RequestValue>();
			if (msg instanceof HttpRequest) {
				HttpRequest request = this.request = (HttpRequest) msg;
				URI uri = new URI(request.getUri());
				path.setLength(0);
                path.append(uri.getPath());
				QueryStringDecoder queryDecoder = new QueryStringDecoder(request.getUri());
				Map<String, List<String>> uriAttributes = queryDecoder.parameters();
			
				for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
					Object[] attrValue = attr.getValue().toArray();

					RequestValue reqValue = null;
					if (attrValue.length == 1) { 
 						reqValue = new RequestValue(RequestValue.ReqestParamsType.STRING, attrValue[0].toString());
					} else if (attrValue.length > 1) {
						String[] strArrVal = new String[attrValue.length];
						for (int i = 0; i < attrValue.length; i++) { 
							strArrVal[i] = attrValue[i].toString();
						}
						reqValue = new RequestValue(RequestValue.ReqestParamsType.STRING_ARRAY, strArrVal);
					} else {
						continue;
					}
					mapParams.put(attr.getKey(), reqValue);
					/*添加需要验证sig的参数*//*
					sigTreeMap.put(attr.getKey(), reqValue.toString());*/
				}

				if (request.getMethod().equals(HttpMethod.GET)) {
					String outString =  RequestProcessor.processHttpRequest(path.toString(), mapParams, "get");
 					outputResponse(ctx, outString);
					return;
				}

				// 判断request请求是否是post请求
				if (request.getMethod().equals(HttpMethod.POST)) {
					try {
						postDecoder = new HttpPostRequestDecoder(factory, request);
					} catch (ErrorDataDecoderException e1) {
						outputResponse(ctx, RequestProcessor.createJSONResult("200001", " post decoder error", null).toString());
						return;
					}
				}
			}

			if (postDecoder != null) {
				if (msg instanceof HttpContent) {
					// New chunk is received
					HttpContent chunk = (HttpContent) msg;
					try {
						postDecoder.offer(chunk);
					} catch (ErrorDataDecoderException e1) {
						outputResponse(ctx, RequestProcessor.createJSONResult("200001", " post offer chunk error", null).toString());
						return;
					}

					try {
						while (postDecoder != null && postDecoder.hasNext()) {
							InterfaceHttpData httpData = postDecoder.next();
							if (httpData != null) {
								try {
									String name = httpData.getName();
									if (InterfaceHttpData.HttpDataType.Attribute == httpData.getHttpDataType()) {
										MixedAttribute attribute = (MixedAttribute) httpData;
										attribute.setCharset(CharsetUtil.UTF_8);
										String value = new String(attribute.getValue().getBytes("utf-8"));
 										mapParams.put(name, new RequestValue(RequestValue.ReqestParamsType.STRING, value));
									} else if (InterfaceHttpData.HttpDataType.FileUpload == httpData.getHttpDataType()) {
										FileUpload fileUpload = (FileUpload) httpData;
										if (fileUpload.isCompleted()) {
											byte[] value = ((FileUpload) httpData).get();
											mapParams.put(name, new RequestValue(RequestValue.ReqestParamsType.BYTEARRAY, value));
										}
									}
								} finally {
									httpData.release();
								}
							}
						}
					} catch (Exception e1) {
						//LOG.error("post decoding error", e1);
					}

					outputResponse(ctx, RequestProcessor.processHttpRequest(path.toString(), mapParams, "post"));
 					return;
				}
			}

		} catch (Exception e) {
			LOG.error("http request processing error", e);
			outputResponse(ctx, RequestProcessor.createJSONResult("200001", " global error", null).toString());
			return;
		}
	}

	private void outputResponse(ChannelHandlerContext ctx, String content) {
		responseContent.setLength(0);
		responseContent.append(content);

		NettyUtil.writeResponse(request, responseContent, ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.channel().close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		try {
			if (msg != null) {
				messageReceived(ctx, msg);
			}
		} catch (Exception e) {
			LOG.error("http request processing error", e);
		} 
	}
}