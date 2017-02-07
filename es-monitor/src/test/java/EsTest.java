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
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 * author:jy
 * time:17-1-18上午11:13
 */
public class EsTest {
    public static void main(String[] args) throws Exception {
        testClient();
    }

    public static void testClient() throws UnknownHostException {
        Settings settings = Settings.builder()
                .put("cluster.name", "sns-api").build();

        // on startup
        TransportClient client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.11"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.12"), 9300))
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.10.9.13"), 9300));


        TermsBuilder interfaceCount = AggregationBuilders.terms("interfaceCount")
                .field("interface.raw")
                .size(100)
                .order(Terms.Order.count(true));

        AvgBuilder interfaceAvgTime = AggregationBuilders.avg("interfaceAvgTime").field("request_time");

        QueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp").gte(new Date(System.currentTimeMillis() - 60 * 60 * 1000)).lte(new Date()))
                .must(QueryBuilders.regexpQuery("interface.raw", "/v[56]/.*"));


//        SortBuilder sort = SortBuilders.fieldSort("order_Date")//排序字段
//                        .order(SortOrder.DESC);//升序或者降序


        SearchResponse searchResponse = client.prepareSearch("logstash-detail-msapi-*")
                .setQuery(qb)
                .addAggregation(interfaceCount.subAggregation(interfaceAvgTime))
                .setExplain(true)
                .execute()
                .actionGet();
        StringTerms interfaceCountAggr = (StringTerms) searchResponse.getAggregations().asMap().get("interfaceCount");
        List<Terms.Bucket> buckets = interfaceCountAggr.getBuckets();

        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        for (Terms.Bucket bucket : buckets) {
            long docCount = bucket.getDocCount();
            Object key = bucket.getKey();
            InternalAvg aggregation = (InternalAvg) bucket.getAggregations().asList().get(0);
            double avg = aggregation.getValue();
            System.out.println(key + "                  _ qps: " + (Double.parseDouble(decimalFormat.format(docCount / 3600.0)) + "_                        " + docCount + "_   " + avg));
        }

       /* System.out.println("=========================================");


        TermsBuilder qpsCount = AggregationBuilders.terms("interfaceCount")
                .field("interface.raw")
                .size(100)
                .order(Terms.Order.count(true));
        DateHistogramBuilder qpsDate = AggregationBuilders.dateHistogram("timestamp").field("@timestamp")
                .interval(DateHistogramInterval.minutes(5)).minDocCount(1);
        SearchResponse searchResponse1 = client.prepareSearch("logstash-detail-msapi-*")
                .setQuery(qb)
                .addAggregation(qpsDate.subAggregation(qpsCount))
                .execute()
                .actionGet();

        StringTerms aggregation = (StringTerms) searchResponse1.getAggregations().asList().get(0);
        List<Terms.Bucket> buckets1 = aggregation.getBuckets();
        for (Terms.Bucket bucket : buckets1) {
            long docCount = bucket.getDocCount();
            Object key = bucket.getKey();
            InternalAvg aggregation = (InternalAvg) bucket.getAggregations().asList().get(0);
            double value = aggregation.getValue();
            System.out.println(key + "_" + docCount + "_" + value);
        }*/


        // on shutdown
        client.close();
    }
}
