package com.sohu.sns.monitor.util;

import com.sohu.sns.monitor.server.dao.BaseTableHbase;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by morgan on 15/10/14.
 */
public class HBaseTableUtil extends BaseTableHbase {
    protected static HTable table = null;
    public  static final String family_name = "monitor_fm";

    /**
     * hbase中建表
     * @param table_name 表名
     * @throws IOException
     */
    public static void createTable(String table_name) throws IOException {
        HBaseAdmin admin = new HBaseAdmin(configuration);
        if (admin.tableExists(table_name)){
            admin.disableTable(table_name);
            admin.deleteTable(table_name);
        }
        HTableDescriptor htd = new HTableDescriptor(TableName.valueOf(table_name));
        HColumnDescriptor cf1 = new HColumnDescriptor(family_name);
        htd.addFamily(cf1);
        admin.createTable(htd);
    }

    public static void createTable(String table_name,String family) throws IOException {
        System.out.println("create table ..." + table_name + "," + family);
        HBaseAdmin admin = new HBaseAdmin(configuration);
        if (!admin.tableExists(table_name)){
            HTableDescriptor htd = new HTableDescriptor(TableName.valueOf(table_name));
            HColumnDescriptor cf1 = new HColumnDescriptor(family);
            htd.addFamily(cf1);
            admin.createTable(htd);
        }
    }


    /**
     * 保存单条记录
     * @param table_name
     * @param rowkey
     * @param family
     * @param qualifier
     * @param value
     * @throws IOException
     */
    public static void saveActionCount(String table_name,String rowkey,String family,String qualifier, String value) throws IOException{
        HTable table = new HTable(configuration, table_name);
        Put p = new Put(rowkey.getBytes());
        p.add(family.getBytes(), qualifier.getBytes(), value.getBytes());
        table.put(p);
        table.close();
    }

    /**
     * 批量保存记录
     * @param table_name
     * @param putList
     * @throws IOException
     */
    public static void savePutList(String table_name, List<Put> putList) throws IOException{
        System.out.println("save put list ... " + table_name + ",putList:" + putList.size());
        HTable table = new HTable(configuration, table_name);
        table.put(putList);
        table.close();
    }

    public static void savePut(String tableName, Put put) throws IOException {
        System.out.println("save put ... " + tableName + ",put:"+put);
        HTable table = new HTable(configuration, tableName);
        table.put(put);
        table.close();
    }

    /**
     * 批量查询
     * @param table_name
     * @param putList
     * @return
     * @throws IOException
     */
    public static Result[] getByGetList(String table_name, List<Get> putList) throws IOException{
        HTable table = new HTable(configuration, table_name);
        Result[] rslist = table.get(putList);
        table.close();
        return rslist;
    }

    /**
     * 根据rowkey删除
     * @param table_name
     * @param rowkey
     * @throws IOException
     */
    public static void deleteRow(String table_name, String rowkey) throws IOException  {
        HTable table = new HTable(configuration, table_name);
        List list = new ArrayList();
        Delete d1 = new Delete(rowkey.getBytes());
        list.add(d1);
        table.delete(list);
        table.close();
    }
}
