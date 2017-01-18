import org.apache.lucene.queryparser.xml.FilterBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

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
                .must(QueryBuilders.rangeQuery("@timestamp").gte(new Date(1484723154040L)).lte(new Date(1484724054040L)))
                .must(QueryBuilders.regexpQuery("interface.raw", "/v[56]/.*"));

        SearchResponse searchResponse = client.prepareSearch("logstash-detail-msapi-*")
                .setQuery(qb)
                .addAggregation(interfaceCount.subAggregation(interfaceAvgTime))
                .setExplain(true)
                .execute()
                .actionGet();
        System.out.println(searchResponse);

        System.out.println("=========================================");


        TermsBuilder qpsCount = AggregationBuilders.terms("interfaceCount")
                .field("interface.raw")
                .size(100)
                .order(Terms.Order.count(true));
        DateHistogramBuilder qpsDate = AggregationBuilders.dateHistogram("timestamp").field("@timestamp")
                .interval(DateHistogramInterval.seconds(1)).minDocCount(1);
        SearchResponse searchResponse1 = client.prepareSearch("logstash-detail-msapi-*")
                .setQuery(qb)
                .addAggregation(qpsDate.subAggregation(qpsCount))
                .execute()
                .actionGet();

        System.out.println(searchResponse1);


        // on shutdown
        client.close();
    }
}
