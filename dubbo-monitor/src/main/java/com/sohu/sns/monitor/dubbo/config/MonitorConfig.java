/**
 * Copyright 2006-2015 handu.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sohu.sns.monitor.dubbo.config;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * MonitorConfig
 *
 * @author Jinkai.Ma
 */
@Configuration
@ComponentScan(basePackages = {"com.sohu.sns.monitor.dubbo"}, includeFilters = {@ComponentScan.Filter(value = Service.class)})
@Import({WebConfig.class, DubboConfig.class, MyBatisConfig.class, Security.class})
public class MonitorConfig {
    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        initZk();
    }

    private void initZk() {
        MutablePropertySources propertySources = ((AbstractEnvironment) env).getPropertySources();

        String envConfig = System.getProperty("env", "TEST2");
        System.out.println(envConfig + "===========================");
        if ("PRODUCT".equals(envConfig)) {
            try {
                Properties properties = parseProperties("/product/application.properties");
                PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("productApp", properties);
                propertySources.addFirst(propertiesPropertySource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("TEST2".equals(envConfig)) {
            try {
                Properties properties = parseProperties("/test2/application.properties");
                PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("productApp", properties);
                propertySources.addFirst(propertiesPropertySource);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private Properties parseProperties(String filePath) throws IOException {
        Properties properties = new Properties();
        properties.load(MonitorConfig.class.getResourceAsStream(filePath));
        return properties;
    }
}
