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

import com.google.common.base.Charsets;
import com.sohu.sns.common.utils.json.JsonMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler implementation for server.
 */
@Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<MqttMessage> {
    private static final JsonMapper mapper = JsonMapper.nonDefaultMapper();
    private static final AtomicInteger userIndex = new AtomicInteger();
    private static final Random userRandom = new Random();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        Object payload = msg.payload();
        if (payload instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) payload;
            System.out.println("payload:" + byteBuf.toString(Charsets.UTF_8));
        }
        System.out.println(msg.toString());

        MqttMessageType mqttMessageType = msg.fixedHeader().messageType();
        switch (mqttMessageType) {
            case PUBLISH: {
                MqttPublishMessage publishMessage = (MqttPublishMessage) msg;
                int messageId = publishMessage.variableHeader().messageId();

                System.out.println(publishMessage.payload().toString(Charsets.UTF_8));
                MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_LEAST_ONCE, false, 0);
                MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(messageId);
                MqttPubAckMessage pubAckMessage = new MqttPubAckMessage(fixedHeader, variableHeader);
                ctx.writeAndFlush(pubAckMessage);

                //test
                MqttPublishVariableHeader publishVariableHeader = new MqttPublishVariableHeader("test", messageId);
                Map<String, Object> replyMessageConent = new HashMap<String, Object>();
                replyMessageConent.put("type", 1);
                replyMessageConent.put("replyId", messageId);
                replyMessageConent.put("data", "{\"deal\":1}");
                String replyMessageStr = mapper.toJson(replyMessageConent);
                ByteBuf replyPayload = Unpooled.copiedBuffer(replyMessageStr, Charsets.UTF_8);
                MqttFixedHeader publishFixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, false, 0);
                MqttPublishMessage replyPublishMessage = new MqttPublishMessage(publishFixedHeader, publishVariableHeader, replyPayload);
                ctx.writeAndFlush(replyPublishMessage);
            }
            break;
            case SUBACK: {
                MqttSubAckMessage subAckMessage = (MqttSubAckMessage) msg;
                MqttSubAckPayload payload1 = subAckMessage.payload();
                MqttMessageIdVariableHeader mqttMessageIdVariableHeader = subAckMessage.variableHeader();

                System.out.println(payload1.grantedQoSLevels());
                System.out.println(mqttMessageIdVariableHeader.messageId());
            }
            default:
                System.out.printf("default " + msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //可变头
        MqttConnectVariableHeader mqttConnectVariableHeader = new MqttConnectVariableHeader(MqttVersion.MQTT_3_1_1.protocolName(), 1 << 2, true, true, false, 0, false, true, 5);

        // 消息体
        String clientIdentifier = getCid();
        String userName = getUserId();
        String password = "{\"gid\":\"01010111060001469e324bb295f2e7c551f6ef60896562ed53111f\",\"token\":\"593f26f3da371043061254ac9e035fcf\",\"passport\":\"huangjingfz009@sohu.com\"}";
        MqttConnectPayload mqttConnectPayLoad = new MqttConnectPayload(clientIdentifier, null, null, userName, password);

        System.out.println("cid:" + clientIdentifier + " userid:" + userName);
        // 固定头
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT, true, MqttQoS.AT_LEAST_ONCE, false, 0);

        MqttConnectMessage mqttMessage = new MqttConnectMessage(mqttFixedHeader, mqttConnectVariableHeader, mqttConnectPayLoad);

        ctx.writeAndFlush(mqttMessage);

        System.out.println(this.getClass().getName() + " channelActive");
        super.channelActive(ctx);
    }

    private String getUserId() {
        return "snstest_" + Math.abs(userRandom.nextInt(10));
    }

    private String getCid() {
        return "snstest_" + Math.abs(userRandom.nextInt(10));
    }

}
