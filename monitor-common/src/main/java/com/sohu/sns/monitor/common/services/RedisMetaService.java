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
     * 获取 meta_redis_used_memory表格记录
     * @return
     */
    public List<RedisMetaMemory> getRedisMeta(){
        return redisMetaDao.getRedisMeta();
    }

    /*
      增加 meta_redis_used_memory表格记录
     */
    public void createRedisMeta(RedisMetaMemory redisMetaMemory){
        redisMetaDao.createRedisMeta(redisMetaMemory);
    }

    /*
      删除 meta_redis_used_memory表格记录
     */
    public void deleteRedisMeta(RedisMetaMemory redisMetaMemory){
        redisMetaDao.deleteRedisMeta(redisMetaMemory);
    }

    /**
     * 更新 meta_redis_used_memory表格记录
     */
    public void updateRedisMeta(RedisMetaMemory redisMetaMemory){
        redisMetaDao.updateRedisMeta(redisMetaMemory);
    }
}
