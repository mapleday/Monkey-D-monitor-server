package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.ErrorLogStat;
import com.sohu.sns.monitor.common.services.AppInfoService;
import com.sohu.sns.monitor.common.services.ErrorLogStatService;
import com.sohu.sns.monitor.es.query.SnsEsQuery;

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

import java.io.*;
import java.net.UnknownHostException;
import java.nio.Buffer;
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

    @RequestMapping("/errorLogStats")
    public String ErrorLogStat() throws IOException {
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
        //获取本地ISO8601格式时间
        String Time[]=errorLogStatService.getISOTime().split("_");
        String currentISOTime=Time[0];
        String startISOTime=Time[1];
        String today=Time[2];
        String indexName = "logstash_snsweb-" + today;
        String type = "logs";
        TermsBuilder appIdTermsBuilder = AggregationBuilders
                .terms("appIdAgg").field("appId")
                .size(0).subAggregation(AggregationBuilders.terms("exception_stat").field("exceptionName.raw").size(0));
        QueryBuilder filterQuery = QueryBuilders
                .rangeQuery("@timestamp").gte(startISOTime)
                .lte(currentISOTime);
        SearchResponse searchResponse = client.prepareSearch(indexName).setTypes(type)
                .setQuery(filterQuery).addAggregation(appIdTermsBuilder)
                .execute().actionGet();
        Map<String, Aggregation> appMap = searchResponse.getAggregations().asMap();
        StringTerms appIds = (StringTerms) appMap.get("appIdAgg");
        Iterator<Terms.Bucket> appIdsBucketIt = appIds.getBuckets().iterator();
        errorLogStatService.handleAppIdsBucketIt(errStatList, appIdsBucketIt,appInfoService);
        map.put("data", errStatList);
        return map;
    }

    @RequestMapping("/updateErrorLogStat")
    @ResponseBody
    public Map updateErrorLogStat(ErrorLogStat errorLogStat) {
        errorLogStatService.updateErrorLogStat(errorLogStat);
        Map<String,Object> map=new HashMap<String,Object>();
        List<ErrorLogStat> list=new ArrayList<ErrorLogStat>();
        list.add(errorLogStat);
        map.put("data",list);
        return map;
    }

}
