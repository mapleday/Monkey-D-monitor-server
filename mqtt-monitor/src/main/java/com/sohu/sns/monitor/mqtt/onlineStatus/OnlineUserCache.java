package com.sohu.sns.monitor.mqtt.onlineStatus;

import org.springframework.stereotype.Component;

/**
 * Created by jy on 16-8-30.
 */
@Component
public class OnlineUserCache {
    /*@Autowired
    RedisCacheDao redisCacheDao;

    *//**
     * 获取在线用户数
     *
     * @return
     *//*
    @Cacheable(value = "onlineUserCache")
    public Set<String> getOnlineUsers() {
        Set<String> onlineUserIds = null;
        try {
            onlineUserIds = redisCacheDao.getOnlineUserIds();
            return onlineUserIds;
        } catch (RedisClusterException e) {
            LOGGER.errorLog(ModuleEnum.SNS_CC_SCHEDULE, "OnlineUserCache.getOnlineUsers", "", "", e);
        }
        return Collections.EMPTY_SET;
    }*/
}
