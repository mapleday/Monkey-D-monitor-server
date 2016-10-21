package com.sohu.sns.monitor.httpmonitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * author:jy
 * time:16-10-13下午7:48
 * json工具类
 */
public class JacksonUtils {
    /**
     * can reuse, share globally
     */
    private static final ObjectMapper object = new ObjectMapper();

    /**
     * can reuse, share globally
     */
    private static final XmlMapper xml = new XmlMapper();

    /**
     * private constructor
     */
    private JacksonUtils() {
    }

    /**
     * return a ObjectMapper that is singleton
     *
     * @return
     */
    public static ObjectMapper getObjectMapper() {
        return object;
    }

    /**
     * return a XmlMapper that is singleton
     *
     * @return
     */
    public static XmlMapper getXmlMapper() {
        return xml;
    }
}
