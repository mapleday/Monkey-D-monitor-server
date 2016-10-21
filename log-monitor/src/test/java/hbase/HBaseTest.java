package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * Created by morgan on 15/10/12.
 */
public class HBaseTest {
    final byte[] cf1 = "cf1".getBytes(Charset.defaultCharset());
    Configuration cfg = null;
    @Before
    public void setUp() {
        cfg = HBaseConfiguration.create();
        cfg.set("hbase.zookeeper.quorum", "10.2.98.72");
//        cfg.set("zookeeper.znode.parent","/hbase2");
//        cfg.set("hbase.master", "10.2.98.72:60000");
//        cfg.set("hbase.client.userprovider.class", "org.apache.hadoop.hbase.security.UserProvider");
    }

    @Test
    public void testCreate() throws Exception {
//        HTable hTable = new HTable(cfg, );

        HBaseAdmin admin = new HBaseAdmin(cfg);
        byte[] table_name = Bytes.toBytes("sns_monitor:monitor_test1");
        if (!admin.tableExists(table_name)){
            HTableDescriptor htd = new HTableDescriptor(TableName.valueOf(table_name));
            byte[] family = Bytes.toBytes("fm1");
            HColumnDescriptor cf1 = new HColumnDescriptor(family);
            htd.addFamily(cf1);
            admin.createTable(htd);
        }

    }

}
