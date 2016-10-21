package com.sohu.sns.monitor.httpmonitor;

import com.sohu.sns.monitor.httpmonitor.dao.httpResource.HttpResourceDAO;
import com.sohu.sns.monitor.httpmonitor.model.HttpResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.List;

/**
 * author:jy
 * time:16-10-14下午3:36
 */
public class APP {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/applicationContext.xml");

        System.in.read();
    }
}
