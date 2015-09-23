package com.sohu.sns.monitor;

import com.sohu.sns.monitor.server.MessageProcessor;
import com.sohu.snscommon.utils.zk.ZkUtils;

/**
 * Created by morgan on 15/9/22.
 */
public class SnsMonitorServer {

    public static void main(String[] args) {
        try {
            ZkUtils.setZkConfigFilePath(args[0]);
            ZkUtils.initZkConfig(args[0]);

            new MessageProcessor().start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
