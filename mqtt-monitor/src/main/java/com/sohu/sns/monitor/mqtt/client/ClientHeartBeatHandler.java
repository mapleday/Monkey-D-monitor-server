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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Keep reconnecting to the server while printing out the current uptime and
 * connection attempt getStatus.
 */
@Sharable
public class ClientHeartBeatHandler extends SimpleChannelInboundHandler<Object> {

    long startTime = -1;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (startTime < 0) {
            startTime = System.currentTimeMillis();
        }
        println("Connected to: " + ctx.channel().remoteAddress());
        ctx.channel().writeAndFlush(SimpleMqttMessage.createPing());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(this.getClass().getName() + ":" + msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }

        IdleStateEvent e = (IdleStateEvent) evt;
        IdleState state = e.state();
        switch (state) {
            case READER_IDLE:
            case WRITER_IDLE:
            case ALL_IDLE:
                ctx.writeAndFlush(SimpleMqttMessage.createPing());
                break;
            default:
                System.out.println(e);
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        println("Disconnected from: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        println("channelUnregistered");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    void println(String msg) {
        if (startTime < 0) {
            System.err.format("[SERVER IS DOWN] %s%n", msg);
        } else {
            System.err.format("[CLIENT: %5ds] %s%n", (System.currentTimeMillis() - startTime) / 1000, msg);
        }
    }
}
