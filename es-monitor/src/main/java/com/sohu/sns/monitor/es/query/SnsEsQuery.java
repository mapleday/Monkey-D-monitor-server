package com.sohu.sns.monitor.es.query;

import com.sohu.sns.monitor.es.config.EsBeanConfig;
import com.sohu.sns.monitor.es.module.ErrorLog;
import io.searchbox.core.search.facet.Facet;
import io.searchbox.core.search.facet.TermsFacet;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yw on 2017/3/16.
 */
@Component
public class SnsEsQuery {
//
    private  static  TransportClient client;


    public List<ErrorLog> queryErrorLogs(SearchResponse searchResponse) throws UnknownHostException {


        List<ErrorLog> errorLogsList=new ArrayList<ErrorLog>();

        SearchHits hits=searchResponse.getHits();
        System.out.println("查询到记录"+hits.getTotalHits());
        SearchHit[] searchHits=hits.getHits();
        if (searchHits.length>0){
            for (SearchHit hit:searchHits){
                ErrorLog errorLog=new ErrorLog();
                errorLog.setExceptionName((String)hit.getSource().get("exceptionName"));
                errorLog.setAppId((String)(hit.getSource().get("appId")));
                errorLog.setModule((String)(hit.getSource().get("module")));
                errorLog.setParam((String)(hit.getSource().get("param")));
                errorLog.setReturnValue((String)(hit.getSource().get("returnValue")));
                errorLog.setMethod((String)(hit.getSource().get("method")));
                errorLog.setExceptionDesc((String)(hit.getSource().get("exceptionDesc")));
                errorLog.setStackTrace((String)(hit.getSource().get("stackTrace")));
                errorLog.setTimestamp((String )(hit.getSource().get("@timestamp")));
                errorLog.setVersion((String)hit.getSource().get("@version"));
                errorLogsList.add(errorLog);
            }
        }
        return errorLogsList;
    }

    public static void main(String[] args) throws UnknownHostException {
        SnsEsQuery snsEsQuery=new SnsEsQuery();
        String indexName="logstash_snsweb-2017.03.20";
        String type="logs";
//        QueryBuilder queryBuilder= QueryBuilders.boolQuery().must(QueryBuilders.termQuery("module","sns_cc_dm"));

        client=snsEsQuery.getClient();

        TermsBuilder appIdTermsBuilder= AggregationBuilders.terms("appIdAgg").field("appId").size(0);
        SearchResponse searchResponse=client.prepareSearch(indexName).setTypes("logs")
                .addAggregation(appIdTermsBuilder)
                .execute()
                .actionGet();
        Map<String,Aggregation> appMap=searchResponse.getAggregations().asMap();
        StringTerms appIds=(StringTerms)appMap.get("appIdAgg");
        Iterator<Terms.Bucket> appIdsBucketIt=appIds.getBuckets().iterator();
        int count=0;
        while (appIdsBucketIt.hasNext()){
            Terms.Bucket appIdBucket=appIdsBucketIt.next();
            System.out.println(appIdBucket.getKeyAsString()+"----"+appIdBucket.getDocCount());
            count++;

        }

        System.out.println(count);
    }

    public   TransportClient getClient() throws UnknownHostException {
        if (null==client){
        Settings settings = Settings.builder()
                .put("client.transport.sniff", true)
                .put("cluster.name", "sns-api").build();
        client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.11"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.12"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.13"), 9300));
        }
        return client;

    }

}
