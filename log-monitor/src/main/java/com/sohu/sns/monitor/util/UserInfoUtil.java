package com.sohu.sns.monitor.util;

import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.constant.SigConstants;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import com.sohu.snscommon.utils.service.SignatureUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by jinyingshi on 2015/9/21.
 * 带缓存的用户信息查询
 * 默认地段：userId,userName,avatar,description
 * 1、从缓存查询
 * 2、不存在则查询接口，并缓存
 */
public class UserInfoUtil {
    private static JsonMapper mapper = JsonMapper.nonDefaultMapper();
    public static String userQueryUrl = "http://sns-api.apps.sohuno.com/v5/users/query";
    public static int split = 8;
    public static int timeout = 3000;

    /**
     * 批量获取用户信息
     * @param userIds
     * @param fields
     * @param timeout
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> getUserByHttp(List<String> userIds, List<String> fields, int timeout) throws Exception {
        if (null == userIds || userIds.isEmpty() || null == fields || fields.isEmpty()) {
            return null;
        }
        String userIdStr = StringUtils.join(userIds, ",");
        String fieldStr = StringUtils.join(fields, ",");
        String userInfoStr = null;

        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("user_id", userIdStr);
        params.put("fields", fieldStr);
        String sig = SignatureUtil.createSig(params, SigConstants.SNS_APP_KEY);
        params.put("sig", sig);

        HttpClientUtil httpClientUtil = HttpClientUtil.create(UserInfoUtil.class.getName(), "getUserByHttp", timeout);
        userInfoStr = httpClientUtil.getStringByPost(userQueryUrl, params, new HashMap<String, String>());
        if (userInfoStr != null) {
            HashMap map = mapper.fromJson(userInfoStr, HashMap.class);
            Integer status = Integer.valueOf(map.get("status").toString());
            if (100000 != status) {
                LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "UserInfoUtil.getUser", userInfoStr, null, null);
                return null;
            }
            Map<String, Object> data = (Map<String, Object>) map.get("data");
            List<Map<String, Object>> userList = (List<Map<String, Object>>) data.get("userInfos");
            return userList;
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        UserInfoUtil.getUserByHttp(Arrays.asList("346869583@qq.com"), Arrays.asList("userId", "userName", "mType"), 5000).get(0);
    }
}
