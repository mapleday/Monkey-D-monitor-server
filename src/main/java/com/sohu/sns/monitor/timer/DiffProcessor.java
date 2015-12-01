package com.sohu.sns.monitor.timer;

import com.sohu.sns.monitor.model.MpUserInfo;
import com.sohu.sns.monitor.model.SnsUserInfo;
import com.sohu.sns.monitor.model.UnameInfo;
import com.sohu.sns.monitor.server.config.UNameMysqlClusterService;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * Created by Gary on 2015/12/1.
 */
@Component
public class DiffProcessor {

    private static final String UPDATE_FLAG = "update diff_status set status = ? where id = 1";
    private static final String QUERY_FLAG = "select status from diff_status where id = 1";

    private static final String QUERY_UNAME_INFO = "select * from u_user_info_%d where status = 1 and type <> '0'";
    private static final String QUERY_UNAME_SNS = "select * from u_user_info_%d where status = 1";

    private static final String QUERY_MP_USER = "select * from profile where passport = ? and status = 1";
    private static final String QUERY_SNS_USER = "select * from t_user where user_id = ?";

    private static final String IS_EXIST_UNAME_MP = "select count(1) from uname_mp_diff where passportId = ? and date_str = ?";
    private static final String IS_EXIST_UNAME_SNS = "select count(1) from uname_sns_diff where passportId = ? and date_str = ?";

    private static final String UPDATE_UNAME_MP = "update uname_mp_diff set uname_username = ?, mp_username = ?, mp_type = ?, " +
            "uname_type = ?, diff_type = ?, updateTime = now() where passportId = ? and date_str = ?";
    private static final String UPDATE_UNAME_SNS = "update uname_sns_diff set uname_username = ?, mp_username = ?, mp_type = ?, " +
            "uname_type = ?, diff_type = ?, updateTime = now() where passportId = ? and date_str = ?";

    private static final String INSERT_UNAME_MP = "replace into uname_mp_diff set passportId = ?, uname_username = ?, mp_username = ?, " +
            "mp_type = ?, uname_type = ?, diff_type = ?, date_str = ?, updateTime = now()";
    private static final String INSERT_UNAME_SNS = "replace into uname_sns_diff set passportId = ?, uname_username = ?, mp_username = ?, " +
            "mp_type = ?, uname_type = ?, diff_type = ?, date_str = ?, updateTime = now()";

    @Autowired
    private MysqlClusterService mysqlClusterService;
    @Autowired
    private UNameMysqlClusterService uNameMysqlClusterService;

//    @Scheduled(cron = "0 0/5 * * * ? ")
    @Scheduled(cron = "0 45 0 * * ? ")
    public void handle(){
        try {

            JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
            JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
            int random = new Random().nextInt(10000);
            writeJdbcTemplate.update(UPDATE_FLAG, random);
            Thread.currentThread().sleep(5000);
            Long flag = readJdbcTemplate.queryForObject(QUERY_FLAG, Long.class);
            if(!(random == flag)) {
                return;
            }
            System.out.println("diff compare timer begin >>>>>>>>>>>， time : " + DateUtil.getCurrentTime());
            for(int i=0; i < 256; i++) {
                String queryUnameForMp = String.format(QUERY_UNAME_INFO, i);
                String queryUnameForSns = String.format(QUERY_UNAME_SNS, i);

                JdbcTemplate uNameReadJdbcTemplate = uNameMysqlClusterService.getReadJdbcTemplate(null);
                List uNameInfoForMpList = uNameReadJdbcTemplate.query(queryUnameForMp, new UnameInfoMapper());
                List uNameInfoForSnsList = uNameReadJdbcTemplate.query(queryUnameForSns, new UnameInfoMapper());

                /**遍历uname与mp的不同**/
            int flag1 = 0;
                for(Object obj : uNameInfoForMpList) {
            System.out.println(++flag1);
                    UnameInfo unameInfo = (UnameInfo) obj;
                    JdbcTemplate mpReadJdbcTemplate = SpringContextUtil.getBean("mpReadJdbcTemplate");
                    MpUserInfo mpUserInfo = null;
                    try {
                        mpUserInfo = (MpUserInfo) mpReadJdbcTemplate.queryForObject(QUERY_MP_USER, new MpUserInfoMapper(), unameInfo.getPassportId());
                    } catch (DataAccessException e) {
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "diff.handle", null, null, e);
                        continue;
                    }
            System.out.println(unameInfo.getOriginalUserName()+","+mpUserInfo.getUserName());
            System.out.println(unameInfo.getType()+","+mpUserInfo.getAccountType());
                    saveUnameMpDiffToDB(unameInfo, mpUserInfo);
                }

                /**遍历uname与sns的不同**/
            int flag2 = 0;
                for(Object obj : uNameInfoForSnsList) {
            System.out.println(++flag2);
                    UnameInfo unameInfo = (UnameInfo) obj;
                    JdbcTemplate snsReadJdbcTemplate = SpringContextUtil.getBean("snsReadJdbcTemplate");
                    SnsUserInfo snsUserInfo = null;
                    try {
                        snsUserInfo = (SnsUserInfo) snsReadJdbcTemplate.queryForObject(QUERY_SNS_USER, new SnsUserInfoMapper(), unameInfo.getPassportId());
                    } catch (DataAccessException e) {
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "diff.handle", null, null, e);
                        continue;
                    }
            System.out.println(unameInfo.getOriginalUserName()+","+snsUserInfo.getUserName());
            System.out.println(unameInfo.getType()+","+snsUserInfo.getType());
                    saveUnameSnsDiffToDB(unameInfo, snsUserInfo);
                }
            }
            System.out.println("diff compare timer end >>>>>>>>>>>， time : " + DateUtil.getCurrentTime());
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "diff.handle", null, null, e);
            e.printStackTrace();
        }

    }

    /**
     * 更新uname与sns用户的不同
     * @param unameInfo
     * @param snsUserInfo
     * @throws MysqlClusterException
     */
    private void saveUnameSnsDiffToDB(UnameInfo unameInfo, SnsUserInfo snsUserInfo) throws MysqlClusterException {
        if(null == unameInfo || null == snsUserInfo) return;

        Integer diffType = 3;
        if( ! unameInfo.getOriginalUserName().equals(snsUserInfo.getUserName()) && Integer.parseInt(unameInfo.getType()) == snsUserInfo.getType()) {
            diffType = 0;
        }
        if(unameInfo.getOriginalUserName().equals(snsUserInfo.getUserName()) && ! (Integer.parseInt(unameInfo.getType()) == snsUserInfo.getType())) {
            diffType = 1;
        }
        if( ! unameInfo.getOriginalUserName().equals(snsUserInfo.getUserName()) && ! (Integer.parseInt(unameInfo.getType()) == snsUserInfo.getType())) {
            diffType = 2;
        }
        if(3 == diffType) return;

        JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
        String date_str = DateUtil.getCurrentDate();
        Long count = readJdbcTemplate.queryForObject(IS_EXIST_UNAME_SNS, Long.class, unameInfo.getPassportId(), date_str);
        if(0 == count) {
            writeJdbcTemplate.update(INSERT_UNAME_SNS, unameInfo.getPassportId(), unameInfo.getOriginalUserName(),
                    snsUserInfo.getUserName(), snsUserInfo.getType(), Integer.parseInt(unameInfo.getType()), diffType, date_str);
        } else {
            writeJdbcTemplate.update(UPDATE_UNAME_SNS, unameInfo.getOriginalUserName(), snsUserInfo.getUserName(),
                    snsUserInfo.getType(), Integer.parseInt(unameInfo.getType()), diffType, unameInfo.getPassportId(), date_str);
        }
        LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "saveUnameSnsDiffToDB", null, null);
    }

    /**
     * 保存uname与mp的不同到数据库
     * @param unameInfo
     * @param mpUserInfo
     * @throws MysqlClusterException
     */
    private void saveUnameMpDiffToDB(UnameInfo unameInfo, MpUserInfo mpUserInfo) throws MysqlClusterException {

        if(null == unameInfo || null == mpUserInfo) return;

        Integer uNameType;
        try {
            uNameType = Integer.parseInt(unameInfo.getType());
        } catch (NumberFormatException e) {
            return;
        }
        switch (uNameType) {
            case 1:
                uNameType = 0;
                break;
            case 2:
                uNameType = 1;
                break;
            case 4:
                uNameType = 2;
                break;
        }

        Integer diffType = 3;
        if( ! unameInfo.getOriginalUserName().equals(mpUserInfo.getUserName()) && uNameType == mpUserInfo.getAccountType()) {
            diffType = 0;
        }
        if(unameInfo.getOriginalUserName().equals(mpUserInfo.getUserName()) && ! (uNameType == mpUserInfo.getAccountType())) {
            diffType = 1;
        }
        if( ! unameInfo.getOriginalUserName().equals(mpUserInfo.getUserName()) && ! (uNameType == mpUserInfo.getAccountType())) {
            diffType = 2;
        }

        if(3 == diffType) return;

        JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
        String date_str = DateUtil.getCurrentDate();
        Long count = readJdbcTemplate.queryForObject(IS_EXIST_UNAME_MP, Long.class, unameInfo.getPassportId(), date_str);
        if(0 == count) {
            writeJdbcTemplate.update(INSERT_UNAME_MP, unameInfo.getPassportId(), unameInfo.getOriginalUserName(),
                    mpUserInfo.getUserName(), mpUserInfo.getAccountType(), Integer.parseInt(unameInfo.getType()), diffType, date_str);
        } else {
            writeJdbcTemplate.update(UPDATE_UNAME_MP, unameInfo.getOriginalUserName(), mpUserInfo.getUserName(),
                    mpUserInfo.getAccountType(), Integer.parseInt(unameInfo.getType()), diffType, unameInfo.getPassportId(), date_str);
        }
        LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "saveUnameMpDiffToDB", null, null);
    }


    private class UnameInfoMapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet resultSet, int i) throws SQLException {
            UnameInfo unameInfo = new UnameInfo();
            unameInfo.setId(resultSet.getInt("id"));
            unameInfo.setPassportId(resultSet.getString("passport_id"));
            unameInfo.setUserName(resultSet.getString("user_name"));
            unameInfo.setOriginalUserName(resultSet.getString("original_user_name"));
            unameInfo.setUserInfo(resultSet.getString("user_info"));
            unameInfo.setStatus(resultSet.getInt("status"));
            unameInfo.setCreatePassportId(resultSet.getString("create_passport_id"));
            unameInfo.setCreateTime(resultSet.getLong("create_time"));
            unameInfo.setUpdatePassportId(resultSet.getString("update_passport_id"));
            unameInfo.setUpdateTime(resultSet.getLong("update_time"));
            unameInfo.setType(resultSet.getString("type"));
            unameInfo.setEx1(resultSet.getString("ex1"));
            unameInfo.setEx2(resultSet.getString("ex2"));
            return unameInfo;
        }
    }

    private class MpUserInfoMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet resultSet, int i) throws SQLException {
            if(null == resultSet) {
                return null;
            }
            MpUserInfo mpUserInfo = new MpUserInfo();
            mpUserInfo.setId(resultSet.getInt("id"));
            mpUserInfo.setUserName(resultSet.getString("username"));
            mpUserInfo.setPassport(resultSet.getString("passport"));
            mpUserInfo.setAccountType(resultSet.getInt("account_type"));
            mpUserInfo.setStatus(resultSet.getInt("status"));
            return mpUserInfo;
        }
    }

    private class SnsUserInfoMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet resultSet, int i) throws SQLException {
            if(null == resultSet) {
                return null;
            }
            SnsUserInfo snsUserInfo = new SnsUserInfo();
            snsUserInfo.setId(resultSet.getInt("id"));
            snsUserInfo.setPassportId(resultSet.getString("user_id"));
            snsUserInfo.setUserName(resultSet.getString("user_name"));
            snsUserInfo.setType(resultSet.getInt("type"));

            return snsUserInfo;
        }
    }
}


