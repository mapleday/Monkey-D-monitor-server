package com.sohu.sns.monitor.es.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * author:jy
 * time:17-1-18下午5:57
 */
@Configuration
public class EsBeanConfig {
    @Bean
    TransportClient client() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", "sns-api").build();
        return TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.11"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.12"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.13"), 9300));
    }
}
