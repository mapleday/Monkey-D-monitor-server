package com.sohu.sns.monitor.mqtt.client;

import io.netty.handler.codec.mqtt.*;

/**
 * Created by jinyingshi on 2016/6/3.
 * 简单MQTT消息生成器
 */
public class SimpleMqttMessage {
    private static volatile MqttMessage ping;

    /**
     * 创建ping消息
     *
     * @return
     */
    public static MqttMessage createPing() {
        if (ping == null) {
            synchronized (SimpleMqttMessage.class) {
                if (ping == null) {
                    MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_MOST_ONCE, false, 0);
                    ping = MqttMessageFactory.newMessage(mqttFixedHeader, null, null);
                }
            }
        }
        return ping;
    }
}
