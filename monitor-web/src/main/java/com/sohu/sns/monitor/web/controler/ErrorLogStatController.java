package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.ErrorLogStat;
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

    @RequestMapping("/errorLogStats")
    public String ErrorLogStat(){
        return "errorLogStats";
    }

    @RequestMapping("/getErrorLogStats")
    @ResponseBody
    public Map<String,Object> getErrorLogStat() throws UnknownHostException {
        Map map=new HashMap<String,Object>();
        List<ErrorLogStat> errStatList=new ArrayList<ErrorLogStat>();
        SnsEsQuery snsEsQuery=new SnsEsQuery();


        //查询最近2小时结果
        Client client=snsEsQuery.getClient();

        Date now=new Date();
        String IOSTimepattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        String dayPattern="yyyy.MM.dd";
        String today=DateFormatUtils.format(now,dayPattern);
        String indexName="logstash_snsweb-"+today;
        String type="logs";
        String currentISOTime=DateFormatUtils.format(now.getTime()-60*60*1000*8,IOSTimepattern);
        String startISOTime=DateFormatUtils.format(new Date(now.getTime()-60*60*1000*8-60*60*1000*2),IOSTimepattern);
        TermsBuilder appIdTermsBuilder=AggregationBuilders.terms("appIdAgg").field("appId")
                        .size(0);
        QueryBuilder filterQuery=QueryBuilders
                        .rangeQuery("@timestamp")
                        .gte(startISOTime)
                        .lte(currentISOTime);
        SearchResponse searchResponse=client.prepareSearch(indexName).setTypes(type)
                .setQuery(filterQuery)
                .addAggregation(appIdTermsBuilder)
                .execute()
                .actionGet();
        Map<String,Aggregation> appMap=searchResponse.getAggregations().asMap();
        StringTerms appIds=(StringTerms)appMap.get("appIdAgg");
        Iterator<Terms.Bucket> appIdsBucketIt=appIds.getBuckets().iterator();
        while (appIdsBucketIt.hasNext()){
            ErrorLogStat errorLogStat=new ErrorLogStat();
            Terms.Bucket appIdBucket=appIdsBucketIt.next();
            errorLogStat.setErrorCount((int)appIdBucket.getDocCount());
            errorLogStat.setAppId(appIdBucket.getKeyAsString());
            errStatList.add(errorLogStat);
            System.out.println(appIdBucket.getKeyAsString()+"----"+appIdBucket.getDocCount());
        }
        map.put("data",errStatList);
        System.out.println(errStatList.get(0));
        return map;
    }

    @RequestMapping("/updateErrorLogStat")
    @ResponseBody
    public void updateErrorLogStat(ErrorLogStat errorLogStat){
        errorLogStatService.updatErrorLogStat(errorLogStat);
    }
}
