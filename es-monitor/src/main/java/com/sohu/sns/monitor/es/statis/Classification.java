package com.sohu.sns.monitor.es.statis;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lc on 17-2-22.
 */
public class Classification {

    private static Map classficationStatis(Date startTime, Date endTime, String classification) throws Exception{

        Settings settings = Settings.builder()
                .put("cluster.name", "sns-api").build();
        //加入es集群地址
        TransportClient client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.11"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.12"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.13"), 9300));
        //根据field的参数进行查询,此处
        TermsBuilder interfaceCount = AggregationBuilders.terms("count")
                .field(classification)
                .order(Terms.Order.count(true));

        QueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp").gte(startTime).lte(endTime));

        SearchResponse searchResponse = client.prepareSearch("apimqservice_statis_test-2017.02.22")  //index
                .setQuery(qb)
                .addAggregation(interfaceCount)
                .setExplain(true)
                .execute()
                .actionGet();


        StringTerms docCountAggr = (StringTerms) searchResponse.getAggregations().asMap().get("count");
        List<Terms.Bucket> buckets = docCountAggr.getBuckets();
        Map<String,Long> dataMap = new HashMap();

        for (Terms.Bucket bucket : buckets) {
            dataMap.put((String) bucket.getKey(),bucket.getDocCount());
        }
        return dataMap;
    }

    public static void main(String[] args) {
        Date startTime = new Date(System.currentTimeMillis()-2*60*60*1000);
        Date endTime = new Date();
        String classification ="type";

        try {
            Map<String,Long> dataMap = classficationStatis(startTime,endTime,classification);
             for(Map.Entry<String,Long> data:dataMap.entrySet()){
                 System.out.println(data.getKey());
                 System.out.println(data.getValue());
             }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
