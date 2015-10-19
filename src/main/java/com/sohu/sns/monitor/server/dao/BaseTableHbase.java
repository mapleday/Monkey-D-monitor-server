package com.sohu.sns.monitor.server.dao;

import com.sohu.sns.monitor.util.IpUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class BaseTableHbase {

    protected static HTable table = null;
	//protected static TableName tableName = TableName.valueOf("m_sns:t_user_ppp");
	private static final String LOCAL_IP = IpUtil.getLocalAddress().getHostAddress();
	private static final String HADOOP_USER = "media-sns";
	/**
	 * kerberos配置文件路径(krb为kerberos配置文件)
	 */
	private static final String KRB5_CONF = "/etc/krb5.conf";
	/**
	 * hadoop集群为我们分配的用户key
	 */
	private static final String KEYTAB = String.format("/home/%s/%s.keytab", HADOOP_USER,HADOOP_USER);
	/**
	 * hadoop集群的用户凭证
	 */
	private static final String KERBEROS_PRINCIPAL = String.format("%s/%s@HERACLES.SOHUNO.COM", HADOOP_USER,LOCAL_IP);
	private static final String KINIT_COMMAND = String.format("kinit -kt %s %s", KEYTAB,KERBEROS_PRINCIPAL);
	/**
	 * @param args
	 */
	protected static Configuration configuration= null;
	protected static HConnection hc= null;
	protected static HTableInterface tb =null;
	static {
        try {
        	System.setProperty("java.security.krb5.conf", KRB5_CONF);
        	Runtime.getRuntime().exec(KINIT_COMMAND);
        	configuration = HBaseConfiguration.create();
        	configuration.set("hadoop.security.authentication", "kerberos");
			UserGroupInformation.setConfiguration(configuration);
			UserGroupInformation.loginUserFromKeytab(KERBEROS_PRINCIPAL, KEYTAB);
			hc = HConnectionManager.createConnection(configuration);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}