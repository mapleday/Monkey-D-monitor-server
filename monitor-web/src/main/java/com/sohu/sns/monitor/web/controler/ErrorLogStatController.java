package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.AppInfo;
import com.sohu.sns.monitor.common.module.ErrorLogStat;
import com.sohu.sns.monitor.common.services.AppInfoService;
import com.sohu.sns.monitor.common.services.ErrorLogStatService;
import com.sohu.sns.monitor.es.query.SnsEsQuery;

import org.apache.commons.lang.time.DateFormatUtils;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by yw on 2017/3/22.
 */
@Controller
public class ErrorLogStatController {
    @Autowired
    private ErrorLogStatService errorLogStatService;

    @Autowired
    private AppInfoService appInfoService;

    private String startISOTime = "", currentISOTime = "", today = "";

    @RequestMapping("/errorLogStats")
    public String ErrorLogStat() {
        return "errorLogStats";
    }

    @RequestMapping("/getErrorLogStats")
    @ResponseBody
    public Map<String, Object> getErrorLogStat() throws UnknownHostException {
        Map map = new HashMap<String, Object>();
        List<ErrorLogStat> errStatList = new ArrayList<ErrorLogStat>();
        SnsEsQuery snsEsQuery = new SnsEsQuery();
        //查询今天凌晨到现在的结果
        Client client = snsEsQuery.getClient();
        //获取本地ISO格式时间
        setISOTime();
        String indexName = "logstash_snsweb-" + today;
        String type = "logs";

        TermsBuilder appIdTermsBuilder = AggregationBuilders.terms("appIdAgg").field("appId")
                .size(0);
        QueryBuilder filterQuery = QueryBuilders
                .rangeQuery("@timestamp")
                .gte(startISOTime)
                .lte(currentISOTime);
        SearchResponse searchResponse = client.prepareSearch(indexName).setTypes(type)
                .setQuery(filterQuery)
                .addAggregation(appIdTermsBuilder)
                .execute()
                .actionGet();
        Map<String, Aggregation> appMap = searchResponse.getAggregations().asMap();
        StringTerms appIds = (StringTerms) appMap.get("appIdAgg");
        Iterator<Terms.Bucket> appIdsBucketIt = appIds.getBuckets().iterator();
        setErrorLogStat(errStatList, appIdsBucketIt);
        map.put("data", errStatList);
        return map;
    }

    @RequestMapping("/updateErrorLogStat")
    @ResponseBody
    public void updateErrorLogStat(ErrorLogStat errorLogStat) {
        errorLogStatService.updatErrorLogStat(errorLogStat);
    }

    public void setErrorLogStat(List<ErrorLogStat> errStatList, Iterator<Terms.Bucket> appIdsBucketIt) {
        while (appIdsBucketIt.hasNext()) {
            ErrorLogStat errorLogStat = new ErrorLogStat();
            Terms.Bucket appIdBucket = appIdsBucketIt.next();
            String appInfo = appIdBucket.getKeyAsString();
            String appIdInstanceInfo[] = appInfo.replace("\"", "").split("_");
            if (appIdInstanceInfo.length > 1) {
                errorLogStat.setInstanceId(appIdInstanceInfo[1]);
            }
            errorLogStat.setAppId(appIdInstanceInfo[0]);
            errorLogStat.setErrorCount((int) appIdBucket.getDocCount());
            List<AppInfo> appInfoList = appInfoService.getAppInfo(appIdInstanceInfo[0]);
            if (!appInfoList.isEmpty()) {
                errorLogStat.setAppName(appInfoList.get(0).getAppName());
                errorLogStat.setAppDeveloper(appInfoList.get(0).getAppDeveloper());
            }
            errStatList.add(errorLogStat);
        }
    }

    public void setISOTime() {
        Calendar todayStartTime = Calendar.getInstance();
        Date now = todayStartTime.getTime();
        todayStartTime.set(Calendar.HOUR_OF_DAY, 0);
        todayStartTime.set(Calendar.MINUTE, 0);
        todayStartTime.set(Calendar.SECOND, 0);
        todayStartTime.set(Calendar.MILLISECOND, 0);
        Date start = todayStartTime.getTime();
        String IOSTimepattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String dayPattern = "yyyy.MM.dd";
        currentISOTime = DateFormatUtils.format(now.getTime() - 60 * 60 * 1000 * 8, IOSTimepattern);
        startISOTime = DateFormatUtils.format(start.getTime() - 60 * 60 * 1000 * 8, IOSTimepattern);
        today = DateFormatUtils.format(now, dayPattern);

    }


}
