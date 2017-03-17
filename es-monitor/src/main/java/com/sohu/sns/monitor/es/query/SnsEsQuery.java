package com.sohu.sns.monitor.es.query;

import com.sohu.sns.monitor.es.config.EsBeanConfig;
import com.sohu.sns.monitor.es.module.ErrorLog;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yw on 2017/3/16.
 */
@Component
public class SnsEsQuery {
//    @Autowired
//    TransportClient client;
    public List<ErrorLog> queryErrorLogs(QueryBuilder queryBuilder, String indexName, String type) throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("client.transport.sniff",true)
                .put("cluster.name", "sns-api").build();
        TransportClient client=TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.11"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.12"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.13"), 9300));

        List<ErrorLog> errorLogsList=new ArrayList<ErrorLog>();
        SearchResponse searchResponse=client.prepareSearch(indexName).setTypes("logs")
                .setQuery(queryBuilder)
                .execute()
                .actionGet();
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
        String indexName="logstash_snsweb-2017.03.16";
        String type="logs";
        QueryBuilder queryBuilder= QueryBuilders.boolQuery().must(QueryBuilders.termQuery("module","sns_cc_dm"));
        List<ErrorLog> errorLogs=snsEsQuery.queryErrorLogs(queryBuilder,indexName,type);
        if (errorLogs==null){
            System.out.println("xx");
            return;
        }
        System.out.println("------"+errorLogs);
    }
}
