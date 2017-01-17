package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.redisMeta.RedisMetaDao;
import com.sohu.sns.monitor.common.module.RedisMetaMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Administrator on 2017/1/9.
 */
@Component
public class RedisMetaService {
    @Autowired
    RedisMetaDao redisMetaDao;

    /**
     * 添加记录
     * @param redisMetaMemory
     */
    public void addRedisMetaMemory(RedisMetaMemory redisMetaMemory){
        redisMetaDao.saveDayRecord(redisMetaMemory);
    }

    /**
     * 修改记录
     * @param redisMetaMemory
     */
    public void updateRedisMetaMemory(RedisMetaMemory redisMetaMemory){
        redisMetaDao.updateDayRecord(redisMetaMemory);
    }

    /**
     * 删除记录
     * @param redisMetaMemory
     */
    public void deleteRedisMetaMemory(RedisMetaMemory redisMetaMemory){
        redisMetaDao.deleteDayRecord(redisMetaMemory);
    }


    public List<RedisMetaMemory> getRedisMetaMemory(){
        return redisMetaDao.getRedisMetaMemory();
    }
}
