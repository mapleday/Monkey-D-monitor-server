package com.sohu.sns.monitor.mqtt.onlinestatus;

import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Created by jy on 16-8-30.
 * 用户在线状态维护消息接收者
 */
@Component
public class OnlineStatusMqListener implements MessageListener {
    @Override
    public void onMessage(Message message) {
        byte[] body = message.getBody();
        LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "OnlineStatusMqListener.listen.byte", new String(body), "");

    }
}
