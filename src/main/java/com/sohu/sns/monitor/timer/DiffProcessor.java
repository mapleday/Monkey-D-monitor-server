package com.sohu.sns.monitor.timer;

import com.mysql.jdbc.MysqlDataTruncation;
import com.sohu.sns.monitor.model.MpUserInfo;
import com.sohu.sns.monitor.model.SnsUserInfo;
import com.sohu.sns.monitor.model.UnameInfo;
import com.sohu.sns.monitor.server.config.UNameMysqlClusterService;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.sns.monitor.util.UserInfoUtil;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Gary on 2015/12/1.
 */
@Component
public class DiffProcessor {

    private static final String UPDATE_FLAG = "update diff_status set status = ? where id = 1";
    private static final String QUERY_FLAG = "select status from diff_status where id = 1";

    private static final String QUERY_UNAME_INFO = "select * from u_user_info_%d where status = 1 and type <> '0'";
    private static final String QUERY_UNAME_SNS = "select * from u_user_info_%d where status = 1";

    private static final String QUERY_MP_USER = "select * from profile where status = 1";
    private static final String QUERY_SNS_USER = "select * from t_user";

    private static final String IS_EXIST_UNAME_MP = "select count(1) from uname_mp_diff where passportId = ? and date_str = ?";
    private static final String IS_EXIST_UNAME_SNS = "select count(1) from uname_sns_diff where passportId = ? and date_str = ?";

    private static final String UPDATE_UNAME_MP = "update uname_mp_diff set uname_username = ?, mp_username = ?, mp_type = ?, " +
            "uname_type = ?, diff_type = ?, updateTime = now() where passportId = ? and date_str = ?";
    private static final String UPDATE_UNAME_SNS = "update uname_sns_diff set uname_username = ?, sns_username = ?, sns_type = ?, " +
            "uname_type = ?, diff_type = ?, updateTime = now() where passportId = ? and date_str = ?";

    private static final String INSERT_UNAME_MP = "replace into uname_mp_diff set passportId = ?, uname_username = ?, mp_username = ?, " +
            "mp_type = ?, uname_type = ?, diff_type = ?, date_str = ?, updateTime = now()";
    private static final String INSERT_UNAME_SNS = "replace into uname_sns_diff set passportId = ?, uname_username = ?, sns_username = ?, " +
            "sns_type = ?, uname_type = ?, diff_type = ?, date_str = ?, updateTime = now()";

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
            Thread.currentThread().sleep(50000);
            Long flag = readJdbcTemplate.queryForObject(QUERY_FLAG, Long.class);
            if(random != flag) {
                return;
            }
            System.out.println("diff compare timer begin >>>>>>>>>>>， time : " + DateUtil.getCurrentTime());

            /**查询出所有的mp用户信息**/
            JdbcTemplate mpReadJdbcTemplate = SpringContextUtil.getBean("mpReadJdbcTemplate");
            Map<String, MpUserInfo> mpUserInfoMap = new HashMap<String, MpUserInfo>();
            List mpUserInfoList = mpReadJdbcTemplate.query(QUERY_MP_USER, new MpUserInfoMapper());
            for(Object obj : mpUserInfoList) {
                MpUserInfo mpUserInfo = (MpUserInfo) obj;
                mpUserInfoMap.put(mpUserInfo.getPassport(), mpUserInfo);
            }

            List<String> uNamePassPorts = new LinkedList<String>(); //存放唯一名passport的容器，便于批量请求接口
            List<UnameInfo> unameInfoList = new LinkedList<UnameInfo>();//存放唯一名用户信息
int timeoutCount = 0;
int totalCount = 0;
            for(int i=0; i < 256; i++) {
                String queryUnameForMp = String.format(QUERY_UNAME_INFO, i);
                String queryUnameForSns = String.format(QUERY_UNAME_SNS, i);

                JdbcTemplate uNameReadJdbcTemplate = uNameMysqlClusterService.getReadJdbcTemplate(null);
                List uNameInfoForMpList = uNameReadJdbcTemplate.query(queryUnameForMp, new UnameInfoMapper());
                List uNameInfoForSnsList = uNameReadJdbcTemplate.query(queryUnameForSns, new UnameInfoMapper());

                /**遍历uname与mp的不同**/
                for(Object obj : uNameInfoForMpList) {
                    UnameInfo unameInfo = (UnameInfo) obj;
                    if(mpUserInfoMap.containsKey(unameInfo.getPassportId())) {
                        saveUnameMpDiffToDB(unameInfo, mpUserInfoMap.get(unameInfo.getPassportId()));
                    }
                }

                /**遍历uname与sns的不同**/
                int currentPos = 0;
                for(Object obj : uNameInfoForSnsList) {
                    UnameInfo unameInfo = (UnameInfo) obj;
                    uNamePassPorts.add(unameInfo.getPassportId());
                    unameInfoList.add(unameInfo);
                    ++ currentPos;
                    if(5 > uNamePassPorts.size() && currentPos < uNameInfoForSnsList.size()) {
                        continue;
                    }
totalCount++;
                    List<Map<String, Object>> list = null;
                    try {
                        list = UserInfoUtil.getUserByHttp(uNamePassPorts, Arrays.asList("userId", "userName", "mType"), 10000);
                    } catch (SocketTimeoutException e) {
                        Thread.currentThread().sleep(2000);
                        list = new ArrayList<Map<String, Object>>();
                        for(String uNamepassPort : uNamePassPorts) {
                            try {
                                list.add(UserInfoUtil.getUserByHttp(Arrays.asList(uNamepassPort), Arrays.asList("userId", "userName", "mType"), 5000).get(0));
                            }catch (SocketTimeoutException e1) {
System.out.println("totalCount:" + totalCount*5 + ", timeoutCount:" + (++timeoutCount)+", passportId : "+uNamepassPort);
                                list.add(null);
                                continue;
                            }
                        }
                    }
                    if(null != list) {
                        for(int j = 0; j < unameInfoList.size(); j++) {
                            if(null == list.get(j)) continue;
                            UnameInfo newUnameInfo = unameInfoList.get(j);
                            Map<String, Object> map = list.get(j);
                            SnsUserInfo snsUserInfo = new SnsUserInfo();
                            snsUserInfo.setPassportId((String) map.get("userId"));
                            snsUserInfo.setUserName((String) map.get("userName"));
                            snsUserInfo.setType((Integer) map.get("mType"));
                            try {
                                saveUnameSnsDiffToDB(newUnameInfo, snsUserInfo);
                            } catch (MysqlDataTruncation e) {
                    System.out.println("Can't insert data : passportId ,unamepassportId : " + newUnameInfo.getPassportId()+"snsUserPassport : " + snsUserInfo.getUserName()) ;
                                continue;
                            }

                        }
                    }
                    uNamePassPorts.clear();
                    unameInfoList.clear();
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
    private void saveUnameSnsDiffToDB(UnameInfo unameInfo, SnsUserInfo snsUserInfo) throws Exception {
        if(null == unameInfo || null == snsUserInfo || null == snsUserInfo.getPassportId()
                || ! snsUserInfo.getPassportId().equals(unameInfo.getPassportId())) return;
        Integer uNameType, snsUserType = 10000;
        try {
            uNameType = Integer.parseInt(unameInfo.getType());
            if(null != snsUserInfo.getType()) {
                snsUserType = snsUserInfo.getType();
            }
        } catch (NumberFormatException e) {
            uNameType = 1000;
        }
        String unameUserName = "uname_unknown", snsUsername = "sns_unknown";
        if(null != unameInfo.getOriginalUserName()) {
            unameUserName = unameInfo.getOriginalUserName();
        }
        if(null != snsUserInfo.getUserName()) {
            snsUsername = snsUserInfo.getUserName();
        }

        Integer diffType = 3;
        if( ! unameUserName.equals(snsUsername) && uNameType == snsUserType) {
            diffType = 0;
        }
        if(unameUserName.equals(snsUsername) && ! (uNameType == snsUserType)) {
            diffType = 1;
        }
        if( ! unameUserName.equals(snsUsername) && ! (uNameType == snsUserType)) {
            diffType = 2;
        }
        if(3 == diffType) return;

        JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
        String date_str = DateUtil.getCurrentDate();
        Long count = readJdbcTemplate.queryForObject(IS_EXIST_UNAME_SNS, Long.class, unameInfo.getPassportId(), date_str);
        if(0 == count) {
            writeJdbcTemplate.update(INSERT_UNAME_SNS, unameInfo.getPassportId(), unameUserName,
                    snsUsername, snsUserType, uNameType, diffType, date_str);
        } else {
            writeJdbcTemplate.update(UPDATE_UNAME_SNS, unameUserName, snsUsername,
                    snsUserType, uNameType, diffType, unameInfo.getPassportId(), date_str);
        }
        LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "saveUnameSnsDiffToDB", null, null);
    }

    /**
     * 保存uname与mp的不同到数据库
     * @param unameInfo
     * @param mpUserInfo
     * @throws MysqlClusterException
     */
    private void saveUnameMpDiffToDB(UnameInfo unameInfo, MpUserInfo mpUserInfo) throws Exception {

        if(null == unameInfo || null == mpUserInfo) return;

        Integer uNameType, mpType = 10000;
        try {
            uNameType = Integer.parseInt(unameInfo.getType());
            if(null != mpUserInfo.getAccountType()) {
                mpType = mpUserInfo.getAccountType();
            }
        } catch (NumberFormatException e) {
            uNameType = 1000;
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

        String unameUserName = "uname_unknown", mpUsername = "sns_unknown";
        if(null != unameInfo.getOriginalUserName()) {
            unameUserName = unameInfo.getOriginalUserName();
        }
        if(null != mpUserInfo.getUserName()) {
            mpUsername = mpUserInfo.getUserName();
        }

        Integer diffType = 3;
        if( ! unameUserName.equals(mpUsername) && uNameType == mpType) {
            diffType = 0;
        }
        if(unameUserName.equals(mpUsername) && ! (uNameType == mpType)) {
            diffType = 1;
        }
        if( ! unameUserName.equals(mpUsername) && ! (uNameType == mpType)) {
            diffType = 2;
        }

        if(3 == diffType) return;

        JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
        String date_str = DateUtil.getCurrentDate();
        Long count = readJdbcTemplate.queryForObject(IS_EXIST_UNAME_MP, Long.class, unameInfo.getPassportId(), date_str);
        if(0 == count) {
            writeJdbcTemplate.update(INSERT_UNAME_MP, unameInfo.getPassportId(), unameUserName,
                    mpUsername, mpType, unameInfo.getType(), diffType, date_str);
        } else {
            writeJdbcTemplate.update(UPDATE_UNAME_MP, unameUserName, mpUsername, mpType, unameInfo.getType(), diffType,
                    unameInfo.getPassportId(), date_str);
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


