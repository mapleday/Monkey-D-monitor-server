package com.sohu.sns.monitor.timer;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.config.ZkPathConfig;
import com.sohu.sns.monitor.model.RedisInfo;
import com.sohu.sns.monitor.model.RedisIns;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.sns.monitor.util.RedisEmailUtil;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;

/**
 * Created by Gary Chan on 2016/4/15.
 */
@Component
public class RedisDataCheckProfessor {

    private static JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();
    private static JavaType collectionType = jsonMapper.contructCollectionType(ArrayList.class, RedisIns.class);
    private static Joiner joiner = Joiner.on("_").skipNulls();
    private static final String CRLF = "\n\r";
    private static final String NONE = "None";
    private static String REDIS_CHECK_URL = "";
    private static String baseEmailUrl = "";
    private static String simpleEmailInterface = "";
    private static String mailTo = "";
    private static boolean isChanged = false;
    private static Map<String, Long> lastRecordBucket;

    public void handle() throws InterruptedException, IOException, KeeperException {

        long begin = System.currentTimeMillis();
        String time = DateUtil.getCurrentMin();

        Map<String, String> redisConfig = getRedisClusterConfig();
        if(null == redisConfig || redisConfig.isEmpty()) return;
        if (REDIS_CHECK_URL.isEmpty()) return;
        System.out.println("redis check start : " + redisConfig.size() + ", time:" + DateUtil.getCurrentTime());
        Set<String> uids = redisConfig.keySet();

        List<String> redisVisitFailedList = new ArrayList<String>();
        Map<String, List<RedisInfo>> redisClusterInfo = new HashMap<String, List<RedisInfo>>();
        Map<String, Long> currentRecordBucket = new HashMap<String, Long>();

        for(String uid : uids) {
            List<RedisIns> redisInses;
            try {
                String redisInsStr = HttpClientUtil.getStringByGet(String.format(REDIS_CHECK_URL, uid), null);
                redisInses = jsonMapper.fromJson(redisInsStr, collectionType);
                if(null == redisInses || redisInses.isEmpty()) {
                    redisVisitFailedList.add(joiner.join(uid, redisConfig.get(uid)));
                    continue;
                }
            } catch (Exception e) {
                redisVisitFailedList.add(joiner.join(uid, redisConfig.get(uid)));
                LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.getStringByGet", null, null, e);
                e.printStackTrace();
                continue;
            }
            if(null == redisInses) continue;
            for(RedisIns redisIns : redisInses) {
                try {
                    String password = redisConfig.get(uid);
                    Jedis jedis = new Jedis(redisIns.getIp(), redisIns.getPort());
                    String result = jedis.auth(password);
                    if(! "OK".equals(result)) {
                        redisVisitFailedList.add(joiner.join(uid, redisIns.getIp(), redisConfig.get(uid)));
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.auth", password, result, null);
                        continue;
                    }
                    RedisInfo redisInfo = infoExtraction(jedis.info(), redisIns.getMaster(), redisIns.getIp());

                    /**分析数据是否一致用**/
                    if(redisClusterInfo.containsKey(uid)) {
                        if(null == redisClusterInfo.get(uid)) {
                            redisClusterInfo.put(uid, Arrays.asList(redisInfo));
                        } else {
                            redisClusterInfo.get(uid).add(redisInfo);
                        }
                    } else {
                        redisClusterInfo.put(uid, Arrays.asList(redisInfo));
                    }

                    /**分析数据变化趋势***/
                    if(1 == redisIns.getMaster()) {
                        currentRecordBucket.put(joiner.join(uid, redisIns.getIp()), redisInfo.getKeys());
                    }
                    //writeJdbcTemplate.update(INSERT_RECORD, uid, redisInfo.getIp(), redisInfo.getIsMaster(), redisInfo.getKeys(), time);
                    jedis.close();
                } catch (Exception e) {
                    redisVisitFailedList.add(joiner.join(uid, redisIns.getIp(), redisConfig.get(uid)));
                    LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.getInfo", null, null, e);
                    e.printStackTrace();
                }
            }

            String redisVisitFailedInfo = formatVisitFailedReidis(redisVisitFailedList);
            String redisKeysNotSameInfo = formatKeysNotSameRedis(redisClusterInfo);
            StringBuilder keyIncr = new StringBuilder();
            StringBuilder keyDecline = new StringBuilder();
            formatKeysChangeExceptionRedis(currentRecordBucket, keyIncr, keyDecline);

            if(!isChanged || null == mailTo || mailTo.isEmpty()) return;

        String keysIncrException = String.format(RedisEmailUtil.GROW_EXCEPTION, keyIncr.toString().equals(CRLF)?
                "None":keyIncr.toString());
        String KeysDeclineException = String.format(RedisEmailUtil.DECLINE_EXCEPTION, keyDecline.toString().
                equals(CRLF)? "None":keyDecline.toString());

        StringBuilder emailContent = new StringBuilder();
        emailContent.append(redisVisitFailedInfo).append(redisKeysNotSameInfo)
                .append(keysIncrException).append(KeysDeclineException);

        Map<String, String> map = new HashMap<String, String>();
        map.put("subject", String.format(RedisEmailUtil.SUBJECT, time));
        map.put("text", emailContent.toString());
        map.put("to", mailTo);
        isChanged = false;
        try {
            HttpClientUtil.getStringByPost(baseEmailUrl + simpleEmailInterface, map, null);
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.senEmail", null, null, e);
            e.printStackTrace();
        }

    }
        System.out.println("execute time : " + (System.currentTimeMillis() - begin));
    }


    /**
     * get config on zk
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws KeeperException
     */
    private Map<String, String> getRedisClusterConfig() throws IOException, InterruptedException, KeeperException {
        ZkUtils zk = new ZkUtils();
        zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);

        String redisConfig = new String(zk.getData(ZkPathConfig.REDIS_CHECK_CONFIG));

        Map<String, Object> map = jsonMapper.fromJson(redisConfig, HashMap.class);

        REDIS_CHECK_URL = (String) map.get("check_url");

        System.out.println(DateUtil.getCurrentTime() + ",redis_config has refreshed : " + redisConfig);

        return (Map<String, String>) map.get("redis_config");
    }

    private RedisInfo infoExtraction(String info, int isMater, String ip) {
        if(Strings.isNullOrEmpty(info)) {
            return null;
        }
        String[] array = StringUtils.split(info, "\r\n");
        RedisInfo redisInfo = new RedisInfo();
        redisInfo.setIp(null == ip?"":ip);
        redisInfo.setIsMaster(isMater);
        for(String line : array) {
            fillObject(line.trim(), redisInfo);
        }
        return redisInfo;
    }

    private void fillObject(String line, RedisInfo redisInfo) {
        if(null == redisInfo || null == line) {
            return;
        }
        if(line.startsWith("maxmemory:")) {
            Long maxMemory = Long.parseLong(line.substring(line.indexOf(":")+1, line.length()));
            redisInfo.setMaxMemory(null == maxMemory?0L:maxMemory);
        }
        if(line.startsWith("connected_clients:")) {
            Long connectedClients = Long.parseLong(line.substring(line.indexOf(":")+1, line.length()));
            redisInfo.setConnectedClients(null == connectedClients?0L:connectedClients);
        }
        if(line.startsWith("used_memory:")) {
            Long usedMemory = Long.parseLong(line.substring(line.indexOf(":")+1, line.length()));
            redisInfo.setUsedMemory(null == usedMemory?0L:usedMemory);
        }
        if(line.startsWith("used_cpu_sys:")) {
            String usedCpu = line.substring(line.indexOf(":")+1, line.length());
            redisInfo.setUsedCpu(null == usedCpu?"":usedCpu);
        }
        if(line.startsWith("db0:keys=")) {
            Long keys = Long.parseLong(line.substring(line.indexOf("=")+1, line.indexOf(",")));
            redisInfo.setKeys(null == keys?0L:keys);
        }
    }

    /**
     * format visit failed redis info
     * @param redisVisitErrorList all visit failed info list
     * @return formatted visit failed info
     */
    private String formatVisitFailedReidis(List<String> redisVisitErrorList) {
        String result;
        if(null == redisVisitErrorList || redisVisitErrorList.isEmpty()) {
            result = String.format(RedisEmailUtil.VISIT_EXCEPTION, NONE);
        } else {
            StringBuilder strBuffer = new StringBuilder(CRLF);
            for(String info : redisVisitErrorList) {
                strBuffer.append(info).append(CRLF);
            }
            result = String.format(RedisEmailUtil.VISIT_EXCEPTION, strBuffer.toString());
            isChanged = true;
        }
        return result;
    }

    /**
     * format key not same info
     * @param map
     * @return
     */
    private String formatKeysNotSameRedis(Map<String, List<RedisInfo>> map) {
        String result;
        if(null == map || map.isEmpty()) {
            result = String.format(RedisEmailUtil.KEYS_EXCEPTION, NONE);
        } else {
            StringBuilder strBuffer = new StringBuilder(CRLF);
            Set<String> uids = map.keySet();
            for(String uid : uids) {
                List<RedisInfo> redisInfoGroup = map.get(uid);
                int num = 1;
                for(int i = 0; i < redisInfoGroup.size()-1; i++) {
                    if(redisInfoGroup.get(i).getKeys() == redisInfoGroup.get(i+1).getKeys()) {
                        num ++;
                    }
                }
                if(redisInfoGroup.size() != num) {
                    strBuffer.append(uid).append(": ");
                    for(RedisInfo redisInfo : redisInfoGroup) {
                        strBuffer.append(redisInfo.getIp()).append(0 == redisInfo.getIsMaster()?"(s)":"(m)")
                                .append(": ").append(redisInfo.getKeys()).append(" | ");
                    }
                    strBuffer.append(CRLF);
                }
            }
            result = String.format(RedisEmailUtil.KEYS_EXCEPTION, strBuffer.toString());
            isChanged = true;
        }
        return result;
    }

    private void formatKeysChangeExceptionRedis(Map<String, Long> map, StringBuilder keysIncr, StringBuilder keysDecline) {
        keysIncr.append(CRLF);
        keysDecline.append(CRLF);

        if(null == map || map.isEmpty()) {
            return;
        }
        if(null == lastRecordBucket) {
            lastRecordBucket = map;
            return;
        }
        Set<String> redisInses = map.keySet();
        for(String redisIns : redisInses) {
            Long curKeys = map.get(redisIns);
            if(lastRecordBucket.containsKey(redisInses)) {
                Long lastKeys = lastRecordBucket.get(redisIns);
                if(curKeys > lastKeys) {
                    double val = (curKeys-lastKeys)/lastKeys.doubleValue();
                    if(val >= 0.1) {
                        keysIncr.append(redisIns).append(":").append(" lastKeys:").
                                append(lastKeys).append(", currentKeys:").append(curKeys)
                                .append(", incr:").append((int) (val * 100)).append("%");
                        keysIncr.append(CRLF);
                        isChanged = true;
                    }
                } else {
                    double val = Math.abs(curKeys - lastKeys)/lastKeys.doubleValue();
                    if(val >= 0.1) {
                        keysIncr.append(redisIns).append(":").append(" lastKeys:").
                                append(lastKeys).append(", currentKeys:").append(curKeys)
                                .append(", decline:").append((int) (val * 100)).append("%");
                        keysIncr.append(CRLF);
                        isChanged = true;
                    }
                }
            } else {
                keysIncr.append(redisIns).append(":").append(" lastKeys:").
                        append("UnKnown").append(", currentKeys:").append(curKeys);
                keysIncr.append(CRLF);
                isChanged = true;
            }
        }
        lastRecordBucket = map;
    }

    public static void initEnv(String monitorUrls, String errorLogConfig) {
        Map<String, String> urls = jsonMapper.fromJson(monitorUrls, HashMap.class);
        Map<String, Object> errorLogConfigMap = jsonMapper.fromJson(errorLogConfig, HashMap.class);
        baseEmailUrl = urls.get("base_url");
        simpleEmailInterface = urls.get("simple_email_interface");
        List<String> emails = (List<String>) errorLogConfigMap.get("mail_to");
        StringBuilder sb = new StringBuilder();
        for(String email : emails) {
            if(sb.length() != 0) {
                sb.append("|");
            }
            sb.append(email);
        }
        mailTo = sb.toString();
    }
}
