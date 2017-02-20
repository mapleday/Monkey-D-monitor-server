import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.es.esresult.PassportEsResultConverter;
import com.sohu.sns.monitor.es.module.PassportEsResult;
import com.sohu.sns.monitor.es.query.PassportEsAnalysis;
import com.sohu.sns.monitor.es.query.PassportEsQuery;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by morgan on 2017/2/17.
 */
public class PassportEsTest {
    public static void main(String[] args) throws Exception {
//        Map<String,PassportEsResult> passportEsResults = PassportEsQuery.getInstance().queryInternalPassport(System.currentTimeMillis(),"1m");
//        for (PassportEsResult passportEsResult : passportEsResults.values()) {
//            System.out.println(passportEsResult);
//        }

        List<PassportEsResult> results = PassportEsAnalysis.getInstance().analysisTowDayQpm(1.6f);
        for (PassportEsResult result : results) {
            System.out.println(result);
        }

        List<PassportEsResult> results1 = PassportEsAnalysis.getInstance().analysisTowDayAppKey(2.6f);
        for (PassportEsResult passportEsResult : results1) {
            System.out.println(passportEsResult);
        }
    }


}
