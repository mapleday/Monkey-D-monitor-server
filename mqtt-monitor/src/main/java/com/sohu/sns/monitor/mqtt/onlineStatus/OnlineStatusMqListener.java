package com.sohu.sns.monitor.mqtt.onlineStatus;

import org.springframework.stereotype.Component;

/**
 * Created by jy on 16-8-30.
 * 用户在线状态维护消息接收者
 */
@Component
public class OnlineStatusMqListener {
    /*private static final JsonMapper mapper = JsonMapper.nonDefaultMapper();
    @Autowired
    RedisCacheDao redisCacheDao;

    *//**
     * 接string类型的mq消息
     *
     * @param message
     *//*
    public void listen(String message) {
        LOGGER.buziLog(ModuleEnum.SNS_CC_SCHEDULE, "OnlineStatusMqListener.listen", message, "");
        OnLineStatusMessage onLineStatusMessage = mapper.fromJson(message, OnLineStatusMessage.class);
        if (onLineStatusMessage == null || onLineStatusMessage.getType() == null) {
            LOGGER.buziLog(ModuleEnum.SNS_CC_SCHEDULE, "OnlineStatusMqListener.listen", message, "message is null");
            return;
        }

        MqTypeCCEnum type = MqTypeCCEnum.mqTypeCCEnum(onLineStatusMessage.getType());
        switch (type) {
            case USER_ONLINE_NOTICE:
                handleOnline(onLineStatusMessage);
                break;
            case USER_OFFLINE_NOTICE:
                handleOffLine(onLineStatusMessage);
                break;
            default:
                handleDefault(onLineStatusMessage);
        }

    }

    *//**
     * 接tyte类型的消息
     *
     * @param message
     *//*
    public void listen(byte[] message) {
        LOGGER.buziLog(ModuleEnum.SNS_CC_SCHEDULE, "OnlineStatusMqListener.listen", String.valueOf(message), "byte");
    }

    *//**
     * 处理上线消息
     *
     * @param message
     * @throws RedisClusterException
     *//*
    private void handleOnline(OnLineStatusMessage message) {
        try {
            String userId = message.getUserId();
            String cid = message.getCid();
            redisCacheDao.saveOnLineUserId(userId, cid);
        } catch (RedisClusterException e) {
            throw new RuntimeException(e);
        }
    }

    *//**
     * 处理下线消息
     *
     * @param message
     * @throws RedisClusterException
     *//*
    private void handleOffLine(OnLineStatusMessage message) {
        try {
            String userId = message.getUserId();
            String cid = message.getCid();
            redisCacheDao.delOffLineCid(cid);
            List<String> cidsByUser = redisCacheDao.getCidsByUser(userId);
            if (cidsByUser == null || cidsByUser.size() == 0) {
                redisCacheDao.delOffLineUser(userId);
            }
        } catch (RedisClusterException e) {
            throw new RuntimeException(e);
        }
    }

    *//**
     * 处理其他消息
     *
     * @param message
     *//*
    private void handleDefault(OnLineStatusMessage message) {
        LOGGER.buziLog(ModuleEnum.SNS_CC_SCHEDULE, "OnlineStatusMqListener.handleDefault", String.valueOf(message), "");
    }*/
}
