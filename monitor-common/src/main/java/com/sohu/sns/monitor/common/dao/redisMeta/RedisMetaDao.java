package com.sohu.sns.monitor.common.dao.redisMeta;

import com.sohu.sns.monitor.common.module.RedisMetaMemory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * author:jy
 * time:16-12-29下午5:10
 * redisMeta
 */
@Repository
public interface RedisMetaDao {
    /**
     * 获取指定日期的记录数
     *
     * @param logDay
     * @return
     */
    public Integer getExistDay(String logDay);

    /**
     * 获取指定日期内存使用情况
     *
     * @param logDay
     * @return
     */
    public Double getLastDayMemory(String logDay);

//    /**
//     * 保存内存使用记录
//     *
//     * @param redisMetaMemory
//     */
//    public void saveDayRecord(RedisMetaMemory redisMetaMemory);

//    /**
//     * 更新内存使用记录
//     *
//     * @param redisMetaMemory
//     */
//    public void updateDayRecord(RedisMetaMemory redisMetaMemory);

//    /**
//     * 删除内存使用记录
//     *
//     * @param redisMetaMemory
//     */
//    public void deleteDayRecord(RedisMetaMemory redisMetaMemory);

    /**
     * 获取 内存使用表中所有数据
     * @return
     */
    public List<RedisMetaMemory> getRedisMeta();

    /**
     * 增加 一条内存使用记录
     */
    public void createRedisMeta(RedisMetaMemory redisMetaMemory);

    /**
     * 删除 一条内存使用记录
     */
    public void deleteRedisMeta(RedisMetaMemory redisMetaMemory);

    /**
     * 更新 一条内存使用记录
     */
    public void updateRedisMeta(RedisMetaMemory redisMetaMemory);
}
