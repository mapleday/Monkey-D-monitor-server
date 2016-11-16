package com.sohu.sns.monitor.mqtt.onlineStatus;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by jy on 16-9-8.
 */
@Component
public class CheckOnlineUsers {
    @Scheduled(fixedRate = 60000)
    public void checkOnline() {

    }
}
