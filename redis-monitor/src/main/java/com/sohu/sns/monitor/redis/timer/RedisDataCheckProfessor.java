package com.sohu.sns.monitor.redis.timer;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.redis.config.MonitorDBConfig;
import com.sohu.sns.monitor.redis.config.MySqlDBConfig;
import com.sohu.sns.monitor.redis.config.ZkPathConfig;
import com.sohu.sns.monitor.redis.model.DiffInfo;
import com.sohu.sns.monitor.redis.model.MemoryInfo;
import com.sohu.sns.monitor.redis.model.RedisInfo;
import com.sohu.sns.monitor.redis.model.RedisIns;
import com.sohu.sns.monitor.redis.util.DateUtil;
import com.sohu.sns.monitor.redis.util.RedisEmailUtil;
import com.sohu.sns.monitor.redis.util.ZipUtils;

import com.sohu.snscommon.dbcluster.config.ClusterChangedPostProcessor;
import com.sohu.snscommon.dbcluster.config.MysqlClusterConfig;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by yzh on 2016/11/1.
 */
@Component
public class RedisDataCheckProfessor {

    private static JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();
    private static JavaType collectionType = jsonMapper.contructCollectionType(ArrayList.class, RedisIns.class);
    private static Joiner joiner = Joiner.on("_").skipNulls();
    private static final String NONE = "None";
    private static final String SEP = "@@@";
    private static String REDIS_CHECK_URL = "";
    private static String baseEmailUrl = "";
    private static String emailInterface = "";
    private static String mailTo = "";
    private static boolean isChanged = false;
    private static String lastCheckTime = "";
    private static Map<String, Integer> lastRecordBucket;
    private static Map<String, Long> lastMemoryRecordBucket;
    private static Map<String, Integer> lastKeyDiffBucket;
    private static Map<String, Map<String, String>> lastRedisIpPortMap;
    private static final String QUERY_IS_EXIST_DAY = "select count(1) from meta_redis_used_memory_day where log_day = ?";
    private static final String QUERY_LAST_DAY_USED_MEMORY = "select used_memory from meta_redis_used_memory_day where log_day = ?";
    private static final String INSERT_DAY_RECORD = "insert into meta_redis_used_memory_day (last_day_used_memory, used_memory, log_day, update_time) " +
            "values (?, ?, ?, now())";
    private static final String UPDATE_DAY_RECORD = "update meta_redis_used_memory_day set used_memory = ?, update_time = now() where log_day = ?";

    //@Autowired(required = false)
    private MysqlClusterService mysqlClusterService;


    public void handle() throws InterruptedException, IOException, KeeperException {
        long begin = System.currentTimeMillis();
        String time = DateUtil.getCurrentMin();

        Map<String, Map<String, String>> redisConfig = getRedisClusterConfig();
        if (null == redisConfig || redisConfig.isEmpty()) return;
        if (REDIS_CHECK_URL.isEmpty()) return;

        System.out.println("redis check start : " + redisConfig.size() + ", time:" + DateUtil.getCurrentTime());


        List<String> redisVisitFailedList = new ArrayList<String>();
        Map<String, List<RedisInfo>> redisClusterInfo = new HashMap<String, List<RedisInfo>>();
        Map<String, RedisInfo> masterInfo = new HashMap<String, RedisInfo>();
        Map<String, Integer> currentRecordBucket = new HashMap<String, Integer>();
        Map<String, Map<String, String>> redisIpPortMap = new HashMap<String, Map<String, String>>();
        checkRedisConfig(redisConfig, redisVisitFailedList, redisClusterInfo, masterInfo,
                currentRecordBucket, redisIpPortMap);
        try {

            MysqlClusterConfig config = new MySqlDBConfig();
            mysqlClusterService =new MysqlClusterServiceImpl(config,ClusterChangedPostProcessor.NOTHING_PROCESSOR);
            mysqlClusterService.init(config);
            //mysqlClusterService = SpringContextUtil.getBean(MysqlClusterServiceImpl.class);
            metaDataChangeAnal(redisClusterInfo);
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.metaDataChangeAnal", null, null, e);
            e.printStackTrace();
        }
        String redisVisitFailedInfo = formatVisitFailedReidis(redisVisitFailedList);
        String redisKeysNotSameInfo = formatKeysNotSameRedis(redisClusterInfo);
        String memoryAnal = formatMemoryAnal(masterInfo);
        StringBuilder keyIncr = new StringBuilder();
        StringBuilder keyDecline = new StringBuilder();
        formatKeysChangeExceptionRedis(currentRecordBucket, keyIncr, keyDecline);
        String ipPortException = checkIpPort(redisIpPortMap);
        if (!isChanged || null == mailTo || mailTo.isEmpty()) {
            System.out.println("execute time : " + (System.currentTimeMillis() - begin));
            return;
        }
        String growException = RedisEmailUtil.boldLine(RedisEmailUtil.GROW_EXCEPTION);
        String declineException = RedisEmailUtil.boldLine(RedisEmailUtil.DECLINE_EXCEPTION);
        String keysIncrException = String.format(growException, keyIncr.toString().
                equals(RedisEmailUtil.CRLF+RedisEmailUtil.CRLF) ? NONE : keyIncr.toString());
        String KeysDeclineException = String.format(declineException, keyDecline.toString().
                equals(RedisEmailUtil.CRLF+RedisEmailUtil.CRLF) ? NONE : keyDecline.toString());

        StringBuilder emailContent = new StringBuilder();
        emailContent.append(String.format(RedisEmailUtil.TIME, time, lastCheckTime)).append(redisVisitFailedInfo).
                append(redisKeysNotSameInfo).append(memoryAnal).append(keysIncrException).append(KeysDeclineException)
                .append(ipPortException);
        System.out.println(String.format(RedisEmailUtil.TIME, time, lastCheckTime)+"=="
        +redisVisitFailedInfo+"=="+redisKeysNotSameInfo+"=="+memoryAnal+"=="+keysIncrException+"=="+KeysDeclineException
                +"=="+ipPortException);
        Map<String, String> map = new HashMap<String, String>();
        map.put("subject", RedisEmailUtil.SUBJECT);
        map.put("text", emailContent.toString());
        map.put("to", mailTo);
        isChanged = false;
        updateZkSwap(time, currentRecordBucket, masterInfo, lastKeyDiffBucket, lastRedisIpPortMap);
        try {
            HttpClientUtil.getStringByPost(baseEmailUrl + emailInterface, map, null);
            System.out.println("mail_to : " + mailTo);
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.senEmail", null, null, e);
            e.printStackTrace();
        }
        System.out.println("execute time : " + (System.currentTimeMillis() - begin));
    }

    private void checkRedisConfig(Map<String, Map<String, String>> redisConfig,
                                  List<String> redisVisitFailedList,
                                  Map<String, List<RedisInfo>> redisClusterInfo,
                                  Map<String, RedisInfo> masterInfo,
                                  Map<String, Integer> currentRecordBucket,
                                  Map<String, Map<String, String>> redisIpPortMap) {
        Set<String> uids = redisConfig.keySet();
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
                LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.getStringByGet",
                        null, null, e);
                e.printStackTrace();
                continue;
            }
            if (null == redisInses) continue;
            StringBuilder masterBuffer = new StringBuilder();
            StringBuilder slaveBuffer = new StringBuilder();
            for (RedisIns redisIns : redisInses) {
                if(1 == redisIns.getMaster()) {
                    masterBuffer.append(redisIns.getIp()).append(":").append(redisIns.getPort());
                } else {
                    if(0 != slaveBuffer.length()) {
                        slaveBuffer.append(",");
                    }
                    slaveBuffer.append(redisIns.getIp()).append(":").append(redisIns.getPort());
                }
                try {
                    Jedis jedis = new Jedis(redisIns.getIp(), redisIns.getPort());
                    String result = jedis.auth(passwd);
                    if (!"OK".equals(result)) {
                        redisVisitFailedList.add(joiner.join(uid, redisIns.getIp(), passwd, desc));
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.auth", passwd,
                                result, null);
                        continue;
                    }
                    RedisInfo redisInfo = infoExtraction(jedis.info(), redisIns.getMaster(), redisIns.getIp(), desc);

                    /**
                     * 添加master的信息
                     */
                    if(1 == redisInfo.getIsMaster()) {
                        masterInfo.put(uid, redisInfo);
                    }

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
            Map<String, String> masterSlaveInfo = new HashMap<String, String>();
            masterSlaveInfo.put("master", masterBuffer.toString());
            masterSlaveInfo.put("slave", slaveBuffer.toString());
            redisIpPortMap.put(uid+"_"+desc, masterSlaveInfo);
        }
    }

    /**
     * 检查ip&端口变化
     * @param map
     * @return
     */
    private String checkIpPort(Map<String, Map<String, String>> map) {
        String ipPortException = RedisEmailUtil.boldLine(RedisEmailUtil.IP_PORT_EXCEPTION);
        String result;
        if(null == map || map.isEmpty() ) {
            result = String.format(ipPortException, NONE);
            return result;
        }
        if(null == lastRedisIpPortMap || lastRedisIpPortMap.isEmpty()) {
            result = String.format(ipPortException, NONE);
            lastRedisIpPortMap = map;
            return result;
        }
        Set<String> uids = map.keySet();
        StringBuilder strBuffer = new StringBuilder(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
        for(String uid : uids) {
            if(!lastRedisIpPortMap.containsKey(uid)) continue;
            String masterInfo = map.get(uid).get("master");
            String slaveInfo = map.get(uid).get("slave");
            String lastMasterInfo = lastRedisIpPortMap.get(uid).get("master");
            String lastSlaveInfo = lastRedisIpPortMap.get(uid).get("slave");
            if(!masterInfo.equals(lastMasterInfo) || !slaveInfo.equals(lastSlaveInfo)) {
                strBuffer.append(RedisEmailUtil.getSpace(6)).append("*").append(uid).append(RedisEmailUtil.CRLF);

                if(!masterInfo.equals(lastMasterInfo)) {
                    strBuffer.append(RedisEmailUtil.getSpace(10)).append("MASTER : last : ").append(lastMasterInfo)
                            .append(" | current : ").append(masterInfo).append(RedisEmailUtil.CRLF);
                }
                if(!slaveInfo.equals(lastSlaveInfo)) {
                    strBuffer.append(RedisEmailUtil.getSpace(10)).append("SLAVE : last : ").append(lastSlaveInfo)
                            .append(" | current : ").append(slaveInfo).append(RedisEmailUtil.CRLF);
                }

                strBuffer.append(RedisEmailUtil.CRLF);
            }
        }
        if(strBuffer.toString().trim().equals("<br><br>")) {
            result = String.format(ipPortException, NONE);
        } else {
            result = String.format(ipPortException, strBuffer.toString());
        }
        lastRedisIpPortMap = map;
        return result;
    }

    /**
     * 更新zk暂存区
     * @param time
     * @param map
     * @throws KeeperException
     * @throws InterruptedException
     */
    private static void updateZkSwap(String time, Map<String, Integer> map,
                                     Map<String, RedisInfo> masterInfo, Map<String, Integer> diffMap,
                                     Map<String, Map<String,String>> ipPortMap)
            throws KeeperException, InterruptedException, IOException {
        if(null == time) {
            time = DateUtil.getCurrentMin();
        }
        if(null == map) {
            map = new HashMap<String, Integer>();
        }
        if(null == diffMap) {
            diffMap = new HashMap<String, Integer>();
        }
        Map<String, String> lastMemoryInfo = new HashMap<String, String>();
        if(null != masterInfo) {
            Set<String> uids = masterInfo.keySet();
            for(String uid : uids) {
                lastMemoryInfo.put(uid, String.valueOf(masterInfo.get(uid).getUsedMemory()));
            }
        }
        if(null == ipPortMap) {
            ipPortMap = new HashMap<String, Map<String, String>>();
        }

        String swap = time + SEP + jsonMapper.toJson(map) + SEP + jsonMapper.toJson(lastMemoryInfo) + SEP +
                jsonMapper.toJson(diffMap) + SEP + jsonMapper.toJson(ipPortMap);
        ZkUtils zk = new ZkUtils();
        zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
        zk.setData(ZkPathConfig.REDIS_CHECK_SWAP, ZipUtils.gzip(swap).getBytes(), -1);
        zk.close();
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
            StringBuilder strBuffer = new StringBuilder(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
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
            StringBuilder strBuffer = new StringBuilder(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
            Set<String> uids = map.keySet();
            Map<String, Integer> diffMap = new HashMap<String, Integer>();
            List<DiffInfo> list = new LinkedList<DiffInfo>();
            for (String uid : uids) {
                List<RedisInfo> redisInfoGroup = map.get(uid);
                int minSlaveKeys = Integer.MAX_VALUE, maxSlaveKeys = Integer.MIN_VALUE, masterKeys = 0;
                for (RedisInfo redisInfo : redisInfoGroup) {
                    if(redisInfo.getKeys() < minSlaveKeys && 1 != redisInfo.getIsMaster()) {
                        minSlaveKeys = redisInfo.getKeys();
                    }
                    if(redisInfo.getKeys() > maxSlaveKeys && 1 != redisInfo.getIsMaster()) {
                        maxSlaveKeys = redisInfo.getKeys();
                    }
                    if(1 == redisInfo.getIsMaster()) {
                        masterKeys = redisInfo.getKeys();
                    }
                }

                int diff1 = masterKeys - minSlaveKeys;
                int diff2 = masterKeys - maxSlaveKeys;
                int maxDiff = Math.abs(diff1) > Math.abs(diff2)?diff1:diff2;
                diffMap.put(uid, maxDiff);
                int lastMaxDiff = (null == lastKeyDiffBucket.get(uid)?0:lastKeyDiffBucket.get(uid));
                if (0 != diff1 || 0 != diff2) {
                    StringBuilder temp = new StringBuilder();
                    temp.append(RedisEmailUtil.getSpace(6)).append("*").append(uid).append(" (" + map.get(uid).get(0).getDesc() + ")").append(" : ");
                    DiffInfo diffInfo = new DiffInfo();
                    diffInfo.setUid(temp.toString());
                    diffInfo.setList(redisInfoGroup);
                    diffInfo.setMaxDiff(maxDiff);
                    diffInfo.setDiffByLast(maxDiff - lastMaxDiff);
                    list.add(diffInfo);
                }
            }
            Collections.sort(list);
            for(DiffInfo diffInfo : list) {
                strBuffer.append(RedisEmailUtil.colorLine(diffInfo.getUid(), "red")).append(RedisEmailUtil.CRLF);
                List<RedisInfo> temp = diffInfo.getList();
                strBuffer.append(RedisEmailUtil.getSpace(10));
                for (RedisInfo redisInfo : temp) {
                    strBuffer.append(redisInfo.getIp()).append(0 == redisInfo.getIsMaster() ? "(s)" : "(m)")
                            .append(" : ").append(redisInfo.getKeys()).append("&nbsp; &nbsp;|&nbsp; &nbsp;");
                }
                strBuffer.append("Max Diff : ").append(diffInfo.getMaxDiff());
                strBuffer.append("&nbsp; &nbsp;|&nbsp; &nbsp; Diff_By_Last : ").append(diffInfo.getDiffByLast());
                strBuffer.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
            }
            lastKeyDiffBucket = diffMap;
            result = String.format(KEYS_EXCEPTION, strBuffer.toString());
            isChanged = true;
        }
        return result;
    }

    /**
     * 分析redis中master Memory使用情况
     * @param masterInfos
     * @return
     */
    private String formatMemoryAnal(Map<String, RedisInfo> masterInfos) {
        String memoryAnal = RedisEmailUtil.boldLine(RedisEmailUtil.MEMORY_CHANGE_INFO);
        String result;
        if(null == masterInfos || masterInfos.isEmpty()) {
            result = String.format(memoryAnal, NONE);
        } else {
            StringBuilder strBuffer = new StringBuilder(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
            Set<String> uids = masterInfos.keySet();
            List<MemoryInfo> list = new LinkedList<MemoryInfo>();
            for(String uid : uids) {
                RedisInfo redisInfo = masterInfos.get(uid);
                long lastMemoryInfo = 0;
                if(lastMemoryRecordBucket.containsKey(uid)) {
                    lastMemoryInfo = lastMemoryRecordBucket.get(uid);
                }
                long diff = redisInfo.getUsedMemory() - lastMemoryInfo;
                MemoryInfo memoryInfo = new MemoryInfo();
                StringBuilder temp = new StringBuilder();
                temp.append(RedisEmailUtil.getSpace(6)).append("*").append(uid).append(" (" + redisInfo.getDesc() + ")").append(" : ");
                memoryInfo.setUid(temp.toString());
                memoryInfo.setMaxMemory(parseGb(redisInfo.getMaxMemory()));
                memoryInfo.setUsedMemory(parseGb(redisInfo.getUsedMemory()));
                memoryInfo.setLastUsedMemory(parseGb(lastMemoryInfo));
                memoryInfo.setIncr(parseMb(diff));
                list.add(memoryInfo);

            }
            Collections.sort(list);
            for(MemoryInfo memoryInfo : list) {
                if(Math.abs(memoryInfo.getIncr()) <= 1.0) continue;
                strBuffer.append(RedisEmailUtil.colorLine(memoryInfo.getUid(), "red")).append(RedisEmailUtil.CRLF);
                strBuffer.append(RedisEmailUtil.getSpace(10)).append("Max_Memory : ").append(memoryInfo.getMaxMemory()).append(" GB")
                        .append("&nbsp;&nbsp;|&nbsp;&nbsp;").append("Used_Memory : ").append(memoryInfo.getUsedMemory()).append(" GB")
                        .append("&nbsp;&nbsp;|&nbsp;&nbsp;").append("Last_Used_Memory : ").append(memoryInfo.getLastUsedMemory()).append(" GB")
                        .append("&nbsp;&nbsp;|&nbsp;&nbsp;").append("Incr : ").append(memoryInfo.getIncr()).append(" MB");
                strBuffer.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
            }
            result = String.format(memoryAnal, strBuffer.toString());
        }
        return result;
    }

    private double parseMb(Long num) {
        double result = num/1024.0/1024.0;
        BigDecimal bigDecimal = new BigDecimal(result);
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private double parseGb(Long num) {
        double result = num/1024.0/1024.0/1024.0;
        BigDecimal bigDecimal = new BigDecimal(result);
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    private void formatKeysChangeExceptionRedis(Map<String, Integer> map, StringBuilder keysIncr, StringBuilder keysDecline) {
        keysIncr.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
        keysDecline.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);

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
                    if (val >= 0.02) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(RedisEmailUtil.getSpace(6)).append("*").append(redisIns).append(" : ");
                        keysIncr.append(RedisEmailUtil.colorLine(temp.toString(), "red")).append(RedisEmailUtil.CRLF);
                        keysIncr.append(RedisEmailUtil.getSpace(10)).append(" lastKeys:").append(lastKeys).append(", currentKeys:").append(curKeys)
                                .append(", incr:").append((int) (val * 100)).append("%");
                        keysIncr.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
                        isChanged = true;
                    }
                } else if (curKeys < lastKeys){
                    double val = Math.abs(curKeys - lastKeys) / lastKeys.doubleValue();
                    if (val >= 0.02) {
                        StringBuilder temp = new StringBuilder();
                        temp.append(RedisEmailUtil.getSpace(6)).append("*").append(redisIns).append(" : ");
                        keysDecline.append(RedisEmailUtil.colorLine(temp.toString(), "red")).append(RedisEmailUtil.CRLF);
                        keysDecline.append(RedisEmailUtil.getSpace(10)).append(" lastKeys:").append(lastKeys).append(", currentKeys:").append(curKeys)
                                .append(", decline:").append((int) (val * 100)).append("%");
                        keysDecline.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
                        isChanged = true;
                    }
                }
            } else {
                StringBuilder temp = new StringBuilder();
                temp.append(RedisEmailUtil.getSpace(6)).append("*").append(redisIns).append(" : ");
                keysIncr.append(RedisEmailUtil.colorLine(temp.toString(), "red")).append(RedisEmailUtil.CRLF);
                keysIncr.append(RedisEmailUtil.getSpace(10)).append(" lastKeys:").append("UnKnown").append(", currentKeys:").append(curKeys);
                keysIncr.append(RedisEmailUtil.CRLF).append(RedisEmailUtil.CRLF);
                isChanged = true;
            }
        }
    }

    /**
     * meta库增量检查
     * @param map
     * @throws Exception
     */
    private void metaDataChangeAnal(Map<String, List<RedisInfo>> map) throws Exception {
        long usedMemory = 0L;
        String currentDay = DateUtil.getCurrentDate();
        String lastDay = DateUtil.getLastDay();

        JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
        Set<String> uids = map.keySet();
        for(String uid : uids) {
            List<RedisInfo> redisInfos = map.get(uid);
            for(RedisInfo redisInfo : redisInfos) {
                if(redisInfo.getDesc().matches(".*-meta-.*") || redisInfo.getDesc().matches(".*存储.*")) {
                    usedMemory += redisInfo.getUsedMemory();
                }
            }
        }
        Long count = readJdbcTemplate.queryForObject(QUERY_IS_EXIST_DAY, Long.class, currentDay);
        if(0 == count) {
            Long lastDayCount = readJdbcTemplate.queryForObject(QUERY_IS_EXIST_DAY, Long.class, lastDay);
            double lastDayUsedMemory = 0.0;
            if(0 != lastDayCount) {
                lastDayUsedMemory = readJdbcTemplate.queryForObject(QUERY_LAST_DAY_USED_MEMORY, Double.class, lastDay);
            }
            writeJdbcTemplate.update(INSERT_DAY_RECORD, lastDayUsedMemory, parseGb(usedMemory), currentDay);
        } else {
            writeJdbcTemplate.update(UPDATE_DAY_RECORD, parseGb(usedMemory), currentDay);
        }
    }


    /**
     *
     * @return Map<String, Map<String, String>>
     */
    public Map<String, Map<String, String>> getRedisClusterConfig() throws IOException, InterruptedException, KeeperException {
        ZkUtils zk = new ZkUtils();
        zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
        String swapData = new String(zk.getData(ZkPathConfig.REDIS_CHECK_SWAP));
        String redisConfig = new String(zk.getData(ZkPathConfig.REDIS_CHECK_CONFIG));
        zk.close();

        swapData = ZipUtils.gunzip(swapData);
        String[] array = swapData.split(SEP);
        if(5 != array.length) {
            lastCheckTime = DateUtil.getCurrentMin();
            lastRecordBucket = new HashMap<String, Integer>();
            lastMemoryRecordBucket = new HashMap<String, Long>();
            lastKeyDiffBucket = new HashMap<String, Integer>();
            lastRedisIpPortMap = new HashMap<String, Map<String, String>>();
        }else {
            lastCheckTime = array[0];
            lastRecordBucket = jsonMapper.fromJson(array[1], HashMap.class);
            Map<String, String> temp = jsonMapper.fromJson(array[2], HashMap.class);
            Set<String> uids = temp.keySet();
            Map<String, Long> map = new HashMap<String, Long>();
            for(String uid : uids) {
                map.put(uid, Long.parseLong(temp.get(uid)));
            }
            lastMemoryRecordBucket = map;
            lastKeyDiffBucket = jsonMapper.fromJson(array[3], HashMap.class);
            Map<String, Object> ipPortMap = jsonMapper.fromJson(array[4], HashMap.class);
            Map<String, Map<String, String>> lastIpPortMap = new HashMap<String, Map<String, String>>();
            Set<String> uidDescSet = ipPortMap.keySet();
            for(String str : uidDescSet) {
                lastIpPortMap.put(str, (Map<String, String>) ipPortMap.get(str));
            }
            lastRedisIpPortMap = lastIpPortMap;
        }
        Map<String, Object> map = jsonMapper.fromJson(redisConfig, HashMap.class);
        REDIS_CHECK_URL = (String) map.get("check_url");
        System.out.println(DateUtil.getCurrentTime() + ",redis_config has refreshed : " + redisConfig);
        return (Map<String, Map<String, String>>) map.get("redis_config");
    }
    public static void initEnv(String monitorUrls, String errorLogConfig, String swap, ZkUtils zkUtils) throws KeeperException, InterruptedException {
        if(Strings.isNullOrEmpty(swap)) {
            String time = DateUtil.getCurrentMin();
            Map<String, Long> map  = new HashMap<String, Long>();
            String swapData = time + SEP + jsonMapper.toJson(map) + SEP + jsonMapper.toJson(map) + SEP + jsonMapper.toJson(map) + SEP + jsonMapper.toJson(map);
            zkUtils.setData(ZkPathConfig.REDIS_CHECK_SWAP, ZipUtils.gzip(swapData).getBytes(), -1);
        }
        zkUtils.close();
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
