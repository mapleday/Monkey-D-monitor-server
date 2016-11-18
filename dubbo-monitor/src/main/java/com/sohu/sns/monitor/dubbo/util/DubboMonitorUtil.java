package com.sohu.sns.monitor.dubbo.util;

import com.sohu.sns.monitor.dubbo.domain.DubboInvoke;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yzh on 2016/11/18.
 */
public class DubboMonitorUtil {
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private static String phone = "18910556026,15201017693";

    public static void checkDubboInvoke(DubboInvoke dubboInvoke){
        final DubboInvoke invoke = dubboInvoke;
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if(invoke.getFailure()>0){
                        String msg = formatMsg(invoke);
                        MsgUtil.sendWeixin(phone,msg);
                    }
                } catch (Exception e) {
                    LOGGER.errorLog(ModuleEnum.UTIL, "DubboMonitorUtil.checkDubboInvoke", null, null, e);
                }
            }
        });
    }

    private static String formatMsg(DubboInvoke invoke){
        StringBuilder sb = new StringBuilder("");
        sb.append(new SimpleDateFormat("HH:mm:ss").format(new Date())+"\n");
        sb.append("dubbo 接口方法请求失败: \n");
        sb.append("service: "+invoke.getService()+"\n");
        sb.append("method: "+invoke.getMethod()+"\n");
        sb.append("provider: "+invoke.getProvider()+"\n");
        sb.append("consumer: "+invoke.getConsumer()+"\n");
        sb.append("success num: "+invoke.getSuccess()+"\n");
        sb.append("failure num: "+invoke.getFailure()+"\n");
        return sb.toString();
    }
}
