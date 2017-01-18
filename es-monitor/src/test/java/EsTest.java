import org.apache.lucene.queryparser.xml.FilterBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
                .order(Terms.Order.count(false));
        AvgBuilder interfaceAvgTime = AggregationBuilders.avg("interfaceAvgTime").field("request_time");

        SearchResponse searchResponse = client.prepareSearch("logstash-detail-msapi-*")
                .setQuery(QueryBuilders.prefixQuery("interface.raw", "/v5"))
                .setQuery(QueryBuilders.prefixQuery("interface.raw", "/v6"))
                .setPostFilter(QueryBuilders.rangeQuery("dateRange").from(1484680303001L).to(1484723503001L))
                .addAggregation(interfaceCount.subAggregation(interfaceAvgTime))
                .execute()
                .actionGet();

        System.out.println(searchResponse);


        // on shutdown
        client.close();
    }
}
