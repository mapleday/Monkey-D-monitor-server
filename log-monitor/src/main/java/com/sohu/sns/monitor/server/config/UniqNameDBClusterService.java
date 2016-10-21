package com.sohu.sns.monitor.server.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.snscommon.dbcluster.config.DBClusterConfigFactory;
import com.sohu.snscommon.dbcluster.service.ds.ConsistHashCircle;
import com.sohu.snscommon.dbcluster.service.ds.DataSourceAddConfig;
import com.sohu.snscommon.dbcluster.service.ds.Pair;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gary on 2015/11/30.
 */

@Component("uNameMysqlClusterService")
public class UniqNameDBClusterService {

    private ConsistHashCircle circle = new ConsistHashCircle();
    private List<JdbcTemplate> readJdbcTemplates = new ArrayList<JdbcTemplate>();
    private List<JdbcTemplate> writeJdbcTemplates = new ArrayList<JdbcTemplate>();
    public static int MAX_ACTIVE_ADD = 0;
    public static int INITIAL_ADD = 0;

    public void init(DBClusterConfigFactory cfgFactory) throws Exception{
        JsonMapper mapper = JsonMapper.nonDefaultMapper();
        String dbConfigStr = "{\n" +
                "\t\"read\":[\n" +
                "\t\t{\n" +
                "\t\t\t\"jdbc_driver\":\"com.mysql.jdbc.Driver\",\n" +
                "\t\t\t\"jdbc_url\":\"jdbc:mysql://10.10.18.34:3306/sns_name?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true\",\n" +
                "\t\t\t\"jdbc_username\":\"sns_name\",\n" +
                "\t\t\t\"jdbc_password\":\"rD5qBeoCzepGoFF\",\n" +
                "\t\t\t\"filters\":\"stat\",\n" +
                "\t\t\t\"maxActive\":1000,\n" +
                "\t\t\t\"initialSize\":20,\n" +
                "\t\t\t\"maxWait\":180000,\n" +
                "\t\t\t\"minIdle\":10,\n" +
                "\t\t\t\"#maxIdle\":15,\n" +
                "\t\t\t\"timeBetweenEvictionRunsMillis\":60000,\n" +
                "\t\t\t\"minEvictableIdleTimeMillis\":300000,\n" +
                "\t\t\t\"validationQuery\":\"SELECT 'x'\",\n" +
                "\t\t\t\"testWhileIdle\":\"true\",\n" +
                "\t\t\t\"testOnBorrow\":\"false\",\n" +
                "\t\t\t\"testOnReturn\":\"false\",\n" +
                "\t\t\t\"#poolPreparedStatements\":\"true\",\n" +
                "\t\t\t\"maxOpenPreparedStatements\":20,\n" +
                "\t\t\t\"removeAbandoned\":\"true\",\n" +
                "\t\t\t\"removeAbandonedTimeout\":1800,\n" +
                "\t\t\t\"logAbandoned\":\"true\"\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"write\":[\n" +
                "\t\t{\n" +
                "\t\t\t\"jdbc_driver\":\"com.mysql.jdbc.Driver\",\n" +
                "\t\t\t\"jdbc_url\":\"jdbc:mysql://10.10.18.33:3306/sns_name?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true\",\n" +
                "\t\t\t\"jdbc_username\":\"sns_name\",\n" +
                "\t\t\t\"jdbc_password\":\"rD5qBeoCzepGoFF\",\n" +
                "\t\t\t\"filters\":\"stat\",\n" +
                "\t\t\t\"maxActive\":1000,\n" +
                "\t\t\t\"initialSize\":20,\n" +
                "\t\t\t\"maxWait\":180000,\n" +
                "\t\t\t\"minIdle\":10,\n" +
                "\t\t\t\"#maxIdle\":15,\n" +
                "\t\t\t\"timeBetweenEvictionRunsMillis\":60000,\n" +
                "\t\t\t\"minEvictableIdleTimeMillis\":300000,\n" +
                "\t\t\t\"validationQuery\":\"SELECT 'x'\",\n" +
                "\t\t\t\"testWhileIdle\":\"true\",\n" +
                "\t\t\t\"testOnBorrow\":\"false\",\n" +
                "\t\t\t\"testOnReturn\":\"false\",\n" +
                "\t\t\t\"#poolPreparedStatements\":\"true\",\n" +
                "\t\t\t\"maxOpenPreparedStatements\":20,\n" +
                "\t\t\t\"removeAbandoned\":\"true\",\n" +
                "\t\t\t\"removeAbandonedTimeout\":1800,\n" +
                "\t\t\t\"logAbandoned\":\"true\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        if (dbConfigStr == null) {
            LOGGER.errorLog(ModuleEnum.CLUSTER, "MetaMysqlClusterService.init", null, null, new Exception());
            return;
        }
        Map<String,Object> dbConfigMap = mapper.fromJson(dbConfigStr, HashMap.class);
        if (dbConfigMap != null) {
//            Map<String,Object> readPropMap = (Map<String, Object>) dbConfigMap.get("read-prop");
            List<Map<String,Object>> readList = (List<Map<String, Object>>) dbConfigMap.get("read");

//            Map<String,Object> writePropMap = (Map<String, Object>) dbConfigMap.get("write-prop");
            List<Map<String,Object>> writeList = (List<Map<String, Object>>) dbConfigMap.get("write");

            Map<String, DruidDataSource> dsMap = SpringContextUtil.getBeans(DruidDataSource.class);
            if (readList == null) {
                return;
            }
            for (int i = 0; i < readList.size(); i++) {
                JdbcTemplate readTemplate = createJdbcTemplate(readList.get(0), dsMap.get("uNameReadDataSource_" + i));
                readTemplate.execute("select 1");
                readJdbcTemplates.add(i, readTemplate);
            }
            if( writeList == null){
                //error log
                return ;
            }
            for (int i = 0; i < writeList.size(); i++) {
                JdbcTemplate writeTemplate = createJdbcTemplate(writeList.get(0), dsMap.get("uNameWriteDataSource_" + i));
                writeTemplate.execute("select 1");
                writeJdbcTemplates.add(i, writeTemplate);
            }

            int num = 256;

            int size = readList.size();

            for(int i=0; i < ( num * size ); i++) {
                circle.addInstance(i+"");
            }
        }
    }

    protected JdbcTemplate createJdbcTemplate(Map<String,Object> dbcfg,
                                              DruidDataSource ds) throws SQLException {
        ds.setUrl((String)dbcfg.get("jdbc_url"));
        ds.setUsername((String)dbcfg.get("jdbc_username"));
        ds.setPassword((String)dbcfg.get("jdbc_password"));
        ds.setDriverClassName((String)dbcfg.get("jdbc_driver"));
        ds.setFilters((String)dbcfg.get("filters"));
        ds.setMaxActive((Integer)dbcfg.get("maxActive") + MAX_ACTIVE_ADD);
        ds.setInitialSize((Integer)dbcfg.get("initialSize") + INITIAL_ADD);
        ds.setMaxWait((Integer)dbcfg.get("maxWait") + DataSourceAddConfig.TIME_OUT_ADD);
        ds.setTimeBetweenEvictionRunsMillis((Integer)dbcfg.get("timeBetweenEvictionRunsMillis"));
        ds.setMinEvictableIdleTimeMillis((Integer)dbcfg.get("minEvictableIdleTimeMillis"));
        ds.setValidationQuery((String)dbcfg.get("validationQuery"));
        ds.setTestWhileIdle(Boolean.valueOf((String)dbcfg.get("testWhileIdle")));
        ds.setTestOnBorrow(Boolean.valueOf((String)dbcfg.get("testOnBorrow")));
        ds.setTestOnReturn(Boolean.valueOf((String)dbcfg.get("testOnReturn")));
        ds.setMaxOpenPreparedStatements((Integer)dbcfg.get("maxOpenPreparedStatements"));
        ds.setRemoveAbandoned(Boolean.valueOf((String)dbcfg.get("removeAbandoned")));
        ds.setRemoveAbandonedTimeout((Integer)dbcfg.get("removeAbandonedTimeout"));
        ds.setLogAbandoned(Boolean.valueOf((String)dbcfg.get("logAbandoned")));
        JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
        return jdbcTemplate;
    }

    public String getTblName(String key) {

        return null;
    }

    public DataSource getReadDataSource(String key) {

        return null;
    }

    public DataSource getWriteDataSource(String key) {

        return null;
    }

    public List<DataSource> getReadDataSources(String key) {
        return null;
    }

    public List<DataSource> getWriteDataSources(String key) {
        return null;
    }

    public JdbcTemplate getReadJdbcTemplate(String key) {
        return readJdbcTemplates.get(0);
    }

    public JdbcTemplate getReadJdbcTemplate(String key, int tableNums, int nums) throws MysqlClusterException {
        if (key == null) {
            return null;
        }
        if (checkTableNums(tableNums)) {
            return getReadTemplates().get(getDBIndex(getNodeInt(key,tableNums), tableNums));
        }
        return null;
    }

    public JdbcTemplate getWriteJdbcTemplate(String key) {
        return writeJdbcTemplates.get(0);
    }

    public JdbcTemplate getWriteJdbcTemplate(String key, int tableNums, int nums) throws MysqlClusterException {
        if (key == null) {
            return null;
        }
        if (checkTableNums(tableNums)) {
            return getWriteTemplates().get(getDBIndex(getNodeInt(key,tableNums), tableNums));
        }
        return null;
    }

    public List<JdbcTemplate> getReadJdbcTemplateList() {
        return readJdbcTemplates;
    }

    public List<JdbcTemplate> getWriteJdbcTemplateList() {
        return writeJdbcTemplates;
    }

    public Pair<JdbcTemplate, String> getReadTTInfo(String key, int tableNums, int nums) throws MysqlClusterException {
        if (key == null) {
            return null;
        }
        if (checkTableNums(tableNums)) {
            int nodeIndex = getNodeInt(key,tableNums);
            Pair<JdbcTemplate, String> pair = new Pair<JdbcTemplate, String>();

            pair.setFirst(getReadTemplates().get(getDBIndex(nodeIndex, tableNums)));
            pair.setSecond(nodeIndex+"");
            return pair;
        }
        return null;
    }

    public Pair<JdbcTemplate, String> getReadTTInfo(String key) {
        int nodeIndex = getNodeInt(key);
        Pair<JdbcTemplate, String> pair = new Pair<JdbcTemplate, String>();
        pair.setFirst(getReadTemplates().get(getDBIndex(nodeIndex)));
        pair.setSecond(nodeIndex+"");
        return pair;
    }

    protected List<JdbcTemplate> getReadTemplates() {
        return readJdbcTemplates;
    }

    protected List<JdbcTemplate> getWriteTemplates() {
        return writeJdbcTemplates;
    }

    protected ConsistHashCircle getCircle() {
        return circle;
    }

    private int getDBIndex(int nodeIndex) {
        return nodeIndex / 256;
    }

    private int getDBIndex(int nodeIndex, int tableNums) {
        return nodeIndex / tableNums;
    }

    public Pair<JdbcTemplate, String> getWriteTTInfo(String key) {
        int nodeIndex = getNodeInt(key);
        Pair<JdbcTemplate, String> pair = new Pair<JdbcTemplate, String>();

        pair.setFirst(getWriteTemplates().get(getDBIndex(nodeIndex)));
        pair.setSecond(nodeIndex+"");
        return pair;
    }

    public Pair<JdbcTemplate, String> getWriteTTInfo(String key, int tableNums, int nums) throws MysqlClusterException {
        if (key == null) {
            return null;
        }
        if (checkTableNums(tableNums)) {
            int nodeIndex = getNodeInt(key, tableNums);
            Pair<JdbcTemplate, String> pair = new Pair<JdbcTemplate, String>();

            pair.setFirst(getWriteTemplates().get(getDBIndex(nodeIndex, tableNums)));
            pair.setSecond(nodeIndex+"");
            return pair;
        }
        return null;
    }

    private boolean checkTableNums(int tableNums) throws MysqlClusterException {
        if (tableNums > 256) {
            throw new MysqlClusterException("tableNums mast little than "+256);
        }
        return true;
    }

    public void flushAll() {
    }

    public int getNodeInt(String key, int tableNums) {
        return Math.abs(key.hashCode() % tableNums);
    }

    public int getNodeInt(String key) {
        String node = getCircle().getNode(key);
        int n = Integer.parseInt(node);
        return n;
    }

}
