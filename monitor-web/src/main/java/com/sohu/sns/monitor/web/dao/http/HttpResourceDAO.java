package com.sohu.sns.monitor.web.dao.http;

import com.sohu.sns.monitor.web.domain.HttpResource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * author:jy
 * time:16-10-14上午11:41
 * 资源配置类
 */
@Repository
public interface HttpResourceDAO {
    List<HttpResource> getResources();
}
