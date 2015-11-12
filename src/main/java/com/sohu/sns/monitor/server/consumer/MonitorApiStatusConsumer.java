package com.sohu.sns.monitor.server.consumer;

import com.google.common.base.Function;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.bucket.ApiStatusBucket;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gary on 2015/10/19.
 */
public class MonitorApiStatusConsumer implements Function<byte[], Boolean> {

    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();
    private static final Long MAX_COMPILPERIOD = 1000L;

    @Nullable
    @Override
    public Boolean apply(byte[] bytes) {
        String msg = null;
        try {
            msg = new String(bytes, "UTF-8");
            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "applyApiStatus", msg, null);
            handle(msg);
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "apply.msg.handleApiStatusMsg", msg, null, e);
        }
        return true;
    }

    /**
     * 将api使用情况存入缓冲桶中
     * @param msg
     */
    private void handle(String msg) throws Exception {
        Map<String, Object> msgMap = jsonMapper.fromJson(msg, HashMap.class);
        if(null == msgMap || 0 == msgMap.size()) {
            return;
        }
        String moduleName = (String)msgMap.get("module");
        String method = (String) msgMap.get("method");
        boolean timeOut = Long.valueOf(msgMap.get("compMill").toString()) >= MAX_COMPILPERIOD ? true : false;
        ApiStatusBucket.insertData(moduleName, method, timeOut);
    }
}
