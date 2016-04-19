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
import com.sohu.sns.monitor.util.ZipUtils;
import com.sohu.snscommon.utils.LOGGER;
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
    private static final String NONE = "None";
    private static String REDIS_CHECK_URL = "";
    private static String baseEmailUrl = "";
    private static String emailInterface = "";
    private static String mailTo = "";
    private static boolean isChanged = false;
    private static String lastCheckTime = "";
    private static ZkUtils zk;
    private static Map<String, Integer> lastRecordBucket;

    public void handle() throws InterruptedException, IOException, KeeperException {

        long begin = System.currentTimeMillis();
        String time = DateUtil.getCurrentMin();

        Map<String, Map<String, String>> redisConfig = getRedisClusterConfig();
        if (null == redisConfig || redisConfig.isEmpty()) return;
        if (REDIS_CHECK_URL.isEmpty()) return;
        System.out.println("redis check start : " + redisConfig.size() + ", time:" + DateUtil.getCurrentTime());
        Set<String> uids = redisConfig.keySet();

        List<String> redisVisitFailedList = new ArrayList<String>();
        Map<String, List<RedisInfo>> redisClusterInfo = new HashMap<String, List<RedisInfo>>();
        Map<String, Integer> currentRecordBucket = new HashMap<String, Integer>();

        for (String uid : uids) {
            List<RedisIns> redisInses;
            String passwd = redisConfig.get(uid).get("passwd");
            String desc = redisConfig.get(uid).get("desc");
            try {
                String redisInsStr = HttpClientUtil.getStringByGet(String.format(REDIS_CHECK_URL, uid), null);
                redisInses = jsonMapper.fromJson(redisInsStr, collectionType);
                if (null == redisInses || redisInses.isEmpty()) {
                    redisVisitFailedList.add(joiner.join(uid, passwd, desc));
                    continue;
                }
            } catch (Exception e) {
                redisVisitFailedList.add(joiner.join(uid, passwd, desc));
                LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.getStringByGet", null, null, e);
                e.printStackTrace();
                continue;
            }
            if (null == redisInses) continue;
            for (RedisIns redisIns : redisInses) {
                try {
                    Jedis jedis = new Jedis(redisIns.getIp(), redisIns.getPort());
                    String result = jedis.auth(passwd);
                    if (!"OK".equals(result)) {
                        redisVisitFailedList.add(joiner.join(uid, redisIns.getIp(), passwd, desc));
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.auth", passwd, result, null);
                        continue;
                    }
                    RedisInfo redisInfo = infoExtraction(jedis.info(), redisIns.getMaster(), redisIns.getIp(), desc);

                    /**分析数据是否一致用**/
                    if (redisClusterInfo.containsKey(uid)) {
                        if (null == redisClusterInfo.get(uid)) {
                            List<RedisInfo> temp = new ArrayList<RedisInfo>();
                            temp.add(redisInfo);
                            redisClusterInfo.put(uid, temp);
                        } else {
                            redisClusterInfo.get(uid).add(redisInfo);
                        }
                    } else {
                        List<RedisInfo> temp = new ArrayList<RedisInfo>();
                        temp.add(redisInfo);
                        redisClusterInfo.put(uid, temp);
                    }

                    /**分析数据变化趋势***/
                    if (1 == redisIns.getMaster()) {
                        currentRecordBucket.put(joiner.join(uid, redisIns.getIp(), desc), redisInfo.getKeys());
                    }

                    jedis.close();
                } catch (Exception e) {
                    redisVisitFailedList.add(joiner.join(uid, redisIns.getIp(), passwd));
                    LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.getInfo", null, null, e);
                    e.printStackTrace();
                }
            }
        }
        String redisVisitFailedInfo = formatVisitFailedReidis(redisVisitFailedList);
        String redisKeysNotSameInfo = formatKeysNotSameRedis(redisClusterInfo);
        StringBuilder keyIncr = new StringBuilder();
        StringBuilder keyDecline = new StringBuilder();
        formatKeysChangeExceptionRedis(currentRecordBucket, keyIncr, keyDecline);

        if (!isChanged || null == mailTo || mailTo.isEmpty()) {
            System.out.println("execute time : " + (System.currentTimeMillis() - begin));
            return;
        }
        String growException = RedisEmailUtil.boldLine(RedisEmailUtil.GROW_EXCEPTION);
        String declineException = RedisEmailUtil.boldLine(RedisEmailUtil.DECLINE_EXCEPTION);
        String keysIncrException = String.format(growException, keyIncr.toString().
                equals(RedisEmailUtil.CRLF) ? NONE : keyIncr.toString());
        String KeysDeclineException = String.format(declineException, keyDecline.toString().
                equals(RedisEmailUtil.CRLF) ? NONE : keyDecline.toString());

        StringBuilder emailContent = new StringBuilder();
        emailContent.append(String.format(RedisEmailUtil.TIME, time, lastCheckTime)).append(redisVisitFailedInfo).append(redisKeysNotSameInfo)
                .append(keysIncrException).append(KeysDeclineException);

        Map<String, String> map = new HashMap<String, String>();
        map.put("subject", RedisEmailUtil.SUBJECT);
        map.put("text", emailContent.toString());
        map.put("to", "gordonchen@sohu-inc.com");
        isChanged = false;
        updateZkSwap(time, currentRecordBucket);
        try {
            HttpClientUtil.getStringByPost(baseEmailUrl + emailInterface, map, null);
            System.out.println("mail_to : " + mailTo);
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.senEmail", null, null, e);
            e.printStackTrace();
        }
        System.out.println("execute time : " + (System.currentTimeMillis() - begin));
    }


    /**
     * 更新zk暂存区
     * @param time
     * @param map
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static void updateZkSwap(String time, Map<String, Integer> map) throws KeeperException, InterruptedException {
        if(null == time) {
            time = DateUtil.getCurrentMin();
        }
        if(null == map) {
            map = new HashMap<String, Integer>();
        }
        String swap = time + "|" + jsonMapper.toJson(map);
        zk.setData(ZkPathConfig.REDIS_CHECK_SWAP, ZipUtils.gzip(swap).getBytes(), -1);
    }

    private RedisInfo infoExtraction(String info, int isMater, String ip, String desc) {
        if (Strings.isNullOrEmpty(info)) {
            return null;
        }
        String[] array = StringUtils.split(info, "\n\r");
        RedisInfo redisInfo = new RedisInfo();
        redisInfo.setIp(null == ip ? "" : ip);
        redisInfo.setIsMaster(isMater);
        redisInfo.setDesc(desc);
        for (String line : array) {
            fillObject(line.trim(), redisInfo);
        }
        return redisInfo;
    }

    private void fillObject(String line, RedisInfo redisInfo) {
        if (null == redisInfo || null == line) {
            return;
        }
        if (line.startsWith("maxmemory:")) {
            Long maxMemory = Long.parseLong(line.substring(line.indexOf(":") + 1, line.length()));
            redisInfo.setMaxMemory(null == maxMemory ? 0L : maxMemory);
        }
        if (line.startsWith("connected_clients:")) {
            Long connectedClients = Long.parseLong(line.substring(line.indexOf(":") + 1, line.length()));
            redisInfo.setConnectedClients(null == connectedClients ? 0L : connectedClients);
        }
        if (line.startsWith("used_memory:")) {
            Long usedMemory = Long.parseLong(line.substring(line.indexOf(":") + 1, line.length()));
            redisInfo.setUsedMemory(null == usedMemory ? 0L : usedMemory);
        }
        if (line.startsWith("used_cpu_sys:")) {
            String usedCpu = line.substring(line.indexOf(":") + 1, line.length());
            redisInfo.setUsedCpu(null == usedCpu ? "" : usedCpu);
        }
        if (line.startsWith("db0:keys=")) {
            Integer keys = Integer.parseInt(line.substring(line.indexOf("=") + 1, line.indexOf(",")));
            redisInfo.setKeys(null == keys ? 0 : keys);
        }
    }

    /**
     * format visit failed redis info
     *
     * @param redisVisitErrorList all visit failed info list
     * @return formatted visit failed info
     */
    private String formatVisitFailedReidis(List<String> redisVisitErrorList) {
        String VISIT_EXCEPTION = RedisEmailUtil.boldLine(RedisEmailUtil.VISIT_EXCEPTION);
        String result;
        if (null == redisVisitErrorList || redisVisitErrorList.isEmpty()) {
            result = String.format(VISIT_EXCEPTION, NONE);
        } else {
            StringBuilder strBuffer = new StringBuilder(RedisEmailUtil.CRLF);
            for (String info : redisVisitErrorList) {
                strBuffer.append(RedisEmailUtil.getSpace(6)).append(info).append(RedisEmailUtil.CRLF);
            }
            result = String.format(VISIT_EXCEPTION, strBuffer.toString());
            isChanged = true;
        }
        return result;
    }

    /**
     * format key not same info
     *
     * @param map
     * @return
     */
    private String formatKeysNotSameRedis(Map<String, List<RedisInfo>> map) {
        String KEYS_EXCEPTION = RedisEmailUtil.boldLine(RedisEmailUtil.KEYS_EXCEPTION);
        String result;
        if (null == map || map.isEmpty()) {
            result = String.format(KEYS_EXCEPTION, NONE);
        } else {
            StringBuilder strBuffer = new StringBuilder(RedisEmailUtil.CRLF);
            Set<String> uids = map.keySet();
            for (String uid : uids) {
                List<RedisInfo> redisInfoGroup = map.get(uid);
                Set<Integer> set = new HashSet<Integer>();
                for (RedisInfo redisInfo : redisInfoGroup) {
                    set.add(redisInfo.getKeys());
                }
                if (1 != set.size()) {
                    StringBuilder temp = new StringBuilder();
                    temp.append(RedisEmailUtil.getSpace(6)).append(uid).append(" (" + map.get(uid).get(0).getDesc() + ")").append(" : ");
                    strBuffer.append(RedisEmailUtil.colorLine(temp.toString(), "red")).append(RedisEmailUtil.CRLF);
                    for (RedisInfo redisInfo : redisInfoGroup) {
                        strBuffer.append(RedisEmailUtil.getSpace(10)).append(redisInfo.getIp()).append(0 == redisInfo.getIsMaster() ? "(s)" : "(m)")
                                .append(" : ").append(redisInfo.getKeys()).append("  |  ");
                    }
                    strBuffer.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
                }
            }
            result = String.format(KEYS_EXCEPTION, strBuffer.toString());
            isChanged = true;
        }
        return result;
    }

    private void formatKeysChangeExceptionRedis(Map<String, Integer> map, StringBuilder keysIncr, StringBuilder keysDecline) {
        keysIncr.append(RedisEmailUtil.CRLF);
        keysDecline.append(RedisEmailUtil.CRLF);

        if (null == map || map.isEmpty()) {
            return;
        }
        if (null == lastRecordBucket || lastRecordBucket.isEmpty()) {
            lastRecordBucket = map;
        }
        Set<String> redisInses = map.keySet();
        for (String redisIns : redisInses) {
            Integer curKeys = map.get(redisIns);
            if (lastRecordBucket.containsKey(redisIns)) {
                Integer lastKeys = lastRecordBucket.get(redisIns);
                if (curKeys > lastKeys) {
                    double val = (curKeys - lastKeys) / lastKeys.doubleValue();
                    if (val >= 0.1) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(RedisEmailUtil.getSpace(6)).append(redisIns).append(" : ");
                        keysIncr.append(RedisEmailUtil.colorLine(temp.toString(), "red")).append(RedisEmailUtil.CRLF);
                        keysIncr.append(RedisEmailUtil.getSpace(10)).append(" lastKeys:").append(lastKeys).append(", currentKeys:").append(curKeys)
                                .append(", incr:").append((int) (val * 100)).append("%");
                        keysIncr.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
                        isChanged = true;
                    }
                } else if (curKeys < lastKeys){
                    double val = Math.abs(curKeys - lastKeys) / lastKeys.doubleValue();
                    if (val >= 0.1) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(RedisEmailUtil.getSpace(6)).append(redisIns).append(" : ");
                        keysDecline.append(RedisEmailUtil.colorLine(temp.toString(), "red")).append(RedisEmailUtil.CRLF);
                        keysDecline.append(RedisEmailUtil.getSpace(10)).append(" lastKeys:").append(lastKeys).append(", currentKeys:").append(curKeys)
                                .append(", decline:").append((int) (val * 100)).append("%");
                        keysDecline.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
                        isChanged = true;
                    }
                }
            } else {
                StringBuilder temp = new StringBuilder();
                temp.append(RedisEmailUtil.getSpace(6)).append(redisIns).append(" : ");
                keysIncr.append(RedisEmailUtil.colorLine(temp.toString(), "red")).append(RedisEmailUtil.CRLF);
                keysIncr.append(RedisEmailUtil.getSpace(10)).append(" lastKeys:").append("UnKnown").append(", currentKeys:").append(curKeys);
                keysIncr.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
                isChanged = true;
            }
        }
    }

    /**
     * get config on zk
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws KeeperException
     */
    private Map<String, Map<String, String>> getRedisClusterConfig() throws IOException, InterruptedException, KeeperException {

        String swapData = new String(zk.getData(ZkPathConfig.REDIS_CHECK_SWAP));
        String redisConfig = new String(zk.getData(ZkPathConfig.REDIS_CHECK_CONFIG));

        swapData = ZipUtils.gunzip(swapData);
        lastCheckTime = swapData.substring(0, swapData.indexOf("|"));
        lastRecordBucket = jsonMapper.fromJson(swapData.substring(swapData.indexOf("|")+1, swapData.length()), HashMap.class);

        Map<String, Object> map = jsonMapper.fromJson(redisConfig, HashMap.class);

        REDIS_CHECK_URL = (String) map.get("check_url");

        System.out.println(DateUtil.getCurrentTime() + ",redis_config has refreshed : " + redisConfig);

        return (Map<String, Map<String, String>>) map.get("redis_config");
    }

    public static void initEnv(String monitorUrls, String errorLogConfig, String swap, ZkUtils zkUtils) throws KeeperException, InterruptedException {
        if(null == zk) {
            zk = zkUtils;
        }
        if(Strings.isNullOrEmpty(swap)) {
            String time = DateUtil.getCurrentMin();
            Map<String, Long> map  = new HashMap<String, Long>();
            String swapData = time + "|" + jsonMapper.toJson(map);
            zk.setData(ZkPathConfig.REDIS_CHECK_SWAP, ZipUtils.gzip(swapData).getBytes(), -1);
        }
        Map<String, String> urls = jsonMapper.fromJson(monitorUrls, HashMap.class);
        Map<String, Object> errorLogConfigMap = jsonMapper.fromJson(errorLogConfig, HashMap.class);
        baseEmailUrl = urls.get("base_url");
        emailInterface = urls.get("html_email_interface");
        List<String> emails = (List<String>) errorLogConfigMap.get("mail_to");
        StringBuilder sb = new StringBuilder();
        for (String email : emails) {
            if (sb.length() != 0) {
                sb.append("|");
            }
            sb.append(email);
        }
        mailTo = sb.toString();
    }
}
