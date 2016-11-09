package com.sohu.sns.monitor.common.utils;

import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * author:jy
 * time:16-10-13下午5:20
 * 通知工具类
 */
public class NotifyUtils {
    private static final HttpClientUtil httpClientUtil = HttpClientUtil.create(NotifyUtils.class.getName(), "sendWeixin", 3000);
    private static final String WEI_XIN_URL = "http://sns-mail-sms.apps.sohuno.com/sendSms";

    private NotifyUtils() {

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
}
