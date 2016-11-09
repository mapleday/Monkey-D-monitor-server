package com.sohu.sns.monitor.mqtt.client;

import io.netty.handler.codec.mqtt.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinyingshi on 2016/6/3.
 * 简单MQTT消息生成器
 */
public class SimpleMqttMessage {
    private static volatile MqttMessage ping;

    private SimpleMqttMessage() {
    }

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

    /**
     * 创建订阅消息
     * direct_message 私信
     * sns_log_echo 日志回显
     * sns_notification 小红点通知
     * sns_task 任务
     *
     * @param topics
     * @return
     */
    public static MqttSubscribeMessage createSubscribe(String... topics) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = MqttMessageIdVariableHeader.from(1);
        List<MqttTopicSubscription> subscriptions = new ArrayList<MqttTopicSubscription>();
        for (String topic : topics) {
            subscriptions.add(new MqttTopicSubscription(topic, MqttQoS.AT_LEAST_ONCE));
        }
        MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(subscriptions);
        return new MqttSubscribeMessage(fixedHeader, mqttMessageIdVariableHeader, mqttSubscribePayload);
    }
}
