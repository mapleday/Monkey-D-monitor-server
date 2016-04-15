package com.sohu.sns.monitor.timer;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Strings;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.config.ZkPathConfig;
import com.sohu.sns.monitor.model.RedisInfo;
import com.sohu.sns.monitor.model.RedisIns;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private static String REDIS_CHECK_URL = "";
    private static final String INSERT_RECORD = "replace into redis_monitor set uid = ?, ip = ?, isMaster = ?, keys_count = ?, update_time = ?";

    @Autowired
    private MysqlClusterService mysqlClusterService;

    public void handle() throws InterruptedException, IOException, KeeperException {

        long begin = System.currentTimeMillis();
        String time = DateUtil.getCurrentMin();

        JdbcTemplate readJdbcTemplate = null;
        JdbcTemplate writeJdbcTemplate = null;

        try {
            readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
            writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
        } catch (MysqlClusterException e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.getJdbcTemplate", null, null, e);
            e.printStackTrace();
            return;
        }

        Map<String, String> redisConfig = getRedisClusterConfig();
        if(null == redisConfig || redisConfig.isEmpty()) return;
        if (REDIS_CHECK_URL.isEmpty()) return;
        System.out.println("redis check start : " + redisConfig.size() + ", time:" + DateUtil.getCurrentTime());
        Set<String> uids = redisConfig.keySet();
        List<String> redisNotFoundList = new ArrayList<String>();
        for(String uid : uids) {
            try {
                String redisInsStr = HttpClientUtil.getStringByGet(String.format(REDIS_CHECK_URL, uid), null);
                List<RedisIns> redisInses = jsonMapper.fromJson(redisInsStr, collectionType);
                if(null == redisInses || redisInses.isEmpty()) {
                    redisNotFoundList.add(uid + "_" + redisConfig.get(uid));
                    continue;
                }
                for(RedisIns redisIns : redisInses) {
                    String password = redisConfig.get(uid);
                    Jedis jedis = new Jedis(redisIns.getIp(), redisIns.getPort());
                    String result = jedis.auth(password);
                    if(! "OK".equals(result)) {
                        redisNotFoundList.add(uid + "_" + redisIns.getIp() + "_" + redisConfig.get(uid));
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.auth", password, result, null);
                        continue;
                    }
                    String info = jedis.info();
                    RedisInfo redisInfo = infoExtraction(info, redisIns.getMaster(), redisIns.getIp());
                    writeJdbcTemplate.update(INSERT_RECORD, uid, redisInfo.getIp(), redisInfo.getIsMaster(), redisInfo.getKeys(), time);
                    jedis.close();
                }
            } catch (Exception e) {
                redisNotFoundList.add(uid + "_" + redisConfig.get(uid));
                LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisDataCheckProfessor.handle.jedis", null, null, e);
                e.printStackTrace();
                continue;
            }
        }
        System.out.println("execute time : " + (System.currentTimeMillis() - begin));
    }


    /**
     * 获取zk上面redis的配置
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
}
