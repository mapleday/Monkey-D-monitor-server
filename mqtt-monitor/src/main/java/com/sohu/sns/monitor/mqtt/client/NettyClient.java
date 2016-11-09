/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.sohu.sns.monitor.mqtt.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;


/**
 * Connects to a server periodically to measure and print the uptime of the
 * server.  This example demonstrates how to implement reliable reconnection
 * mechanism in Netty.
 */
public final class NettyClient {
    private static int heartInterval;//心跳间隔
    private static Bootstrap bootstrap;//netty启动入口

    private NettyClient() {

    }

    /**
     * 启动一个连接
     *
     * @param server        服务器地址
     * @param heartInterval 心跳间隔(S)
     * @return
     * @throws InterruptedException
     */
    public static synchronized Channel conn(String server, int heartInterval) throws InterruptedException {
        NettyClient.heartInterval = heartInterval;

        if (bootstrap == null) {
            bootstrap = configureBootstrap();
        }
        String[] split = server.split(":");
        Channel channel = bootstrap.connect(split[0], Integer.parseInt(split[1])).sync().channel();
        ClientConn.getInstance().add(channel);
        return channel;
    }


    private static Bootstrap configureBootstrap() {
        return configureBootstrap(new Bootstrap(), new NioEventLoopGroup());
    }

    /**
     * 配置bootStrap
     *
     * @param b
     * @param g
     * @return
     */
    private static Bootstrap configureBootstrap(Bootstrap b, EventLoopGroup g) {
        b.group(g)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(MqttEncoder.INSTANCE)
                                .addLast(new MqttDecoder())
                                .addLast(new IdleStateHandler(0, 0, heartInterval))
                                .addLast(new NettyClientHandler())
                                .addLast(new ClientHeartBeatHandler());
                    }
                });
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
        return b;
    }

    /**
     * 写到指定的服务器
     *
     * @param channelNodeId
     * @param message
     * @return
     */
    public static ChannelFuture writeToServer(String channelNodeId, Object message) {
        return ClientConn.getInstance().writeAndFlush(channelNodeId, message);
    }

    /**
     *
     *
     * @param message
     * @return
     */
    public static ChannelFuture writeToServerRandom(Object message) {
        return ClientConn.getInstance().writeAndFlushRandom(message);
    }
}
