package com.sohu.sns.monitor.server.consumer;

import com.google.common.base.Function;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.bucket.ApiStatusBucket;
import com.sohu.sns.monitor.model.ApiStatus;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gary on 2015/10/19.
 */
public class MonitorApiStatusConsumer implements Function<byte[], Boolean> {

    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    @Nullable
    @Override
    public Boolean apply(byte[] bytes) {
        String msg = null;
        try {
            msg = new String(bytes, "UTF-8");
            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "applyApiStatus", msg, null);
            handle(msg);
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "apply.msg.handleMsg", msg, null, e);
        }
        return true;
    }

    /**
     * 将api使用情况存入缓冲桶中
     * @param msg
     */
    private void handle(String msg){
        Map<String, Object> msgMap = jsonMapper.fromJson(msg, HashMap.class);
        if(null == msgMap || 0 == msgMap.size()) {
            return;
        }
        ApiStatus apiStatus = new ApiStatus();
        apiStatus.setModuleName((String)msgMap.get("module"));
        apiStatus.setMethodName((String) msgMap.get("method"));
        apiStatus.setParam((String) msgMap.get("param"));
        apiStatus.setReturnValue((String) msgMap.get("returnValue"));
        apiStatus.setCompMill((Long) msgMap.get("compMill"));
        apiStatus.setCacheMill((Long) msgMap.get("cacheMill"));
        apiStatus.setThirdIterMill((Long) msgMap.get("thirdIterMill"));
        apiStatus.setDate(new Date());

        ApiStatusBucket.insertData(apiStatus);
    }
}
