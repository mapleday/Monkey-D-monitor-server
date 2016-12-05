package com.sohu.sns.monitor.mqtt.onlinestatus;

import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

/**
 * Created by jy on 16-8-29.
 * 消息异常处理
 */
public class ErrorMqHandler extends ConditionalRejectingErrorHandler {
    @Override
    public void handleError(Throwable t) {
        LOGGER.errorLog(ModuleEnum.SNS_CC_SCHEDULE, "ErrorMqHandler.HandleError", "", "", new Exception(t));
        super.handleError(t);
    }
}
