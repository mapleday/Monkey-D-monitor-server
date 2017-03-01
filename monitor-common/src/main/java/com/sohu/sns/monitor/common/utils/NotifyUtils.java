package com.sohu.sns.monitor.common.utils;

import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * author:jy
 * time:16-10-13下午5:20
 * 通知工具类
 */

public class NotifyUtils {
    private static final HttpClientUtil httpClientUtil = HttpClientUtil.create(NotifyUtils.class.getName(), "sendWeixin", 3000);
    private static final String WEI_XIN_URL = "http://sns-mail-weixin.sce.sohuno.com/sendSms";

    public NotifyUtils() {

    }

    /**
     * 发送微信通知
     *
     * @param phones
     * @param message
     * @return
     */
    public static boolean sendWeixin(String phones, String message) {
        try {
            Map<String, String> params = new HashMap();
            params.put("phoneNo", phones);
            params.put("msg", message);
            httpClientUtil.getByUtf(WEI_XIN_URL, params);
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.UTIL, "NotifyUtils.sendWeiXin", message, "false", e);
            return false;
        }
        return true;
    }

    /**
     * 发送报警微信
     *
     * @param phones
     * @param message
     * @return
     */
    public static boolean sendAlert(String phones, String message) {
        String messageTemplate = "【SNS报警 %s】%s";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:dd");
        String date = simpleDateFormat.format(new Date());
        return sendWeixin(phones, String.format(messageTemplate, date, message));
    }

    public static void main(String[] args) {
        //sendAlert("18910556026","test");
//        sendAlert("13589038573","test");
        sendAlert("13051807977","mypost!");
    }
}
