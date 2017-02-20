package com.sohu.sns.monitor.es.query;

import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.es.esresult.PassportEsResultConverter;
import com.sohu.sns.monitor.es.module.PassportEsResult;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by morgan on 2017/2/20.
 * passport es 查询请求体模板
 */
public class PassportEsQuery {

    static final PassportEsQuery instance = new PassportEsQuery();

    final String ES_HOST = "192.168.103.81:9200";
    final String QUERY_FORMAT = "http://%s/%s/%s/_search";
    final long MIN_5_IN_MILLIS = 5 * 1000 * 60;

    final String INTERNAL_PASSPORT_INDEX = "logstash-nginx-internal-passport";
    final String INTERNAL_PASSPORT_TYPE = "internal-passport";

    final String PLUS_SOHUNO_INDEX = "logstash-detail-nginx-rest-plus-sohuno";
    final String PLUS_SOHUNO_TYPE = "detail-plus-sohuno";

    JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    HttpClientUtil httpClientUtil = new HttpClientUtil(4, 10000, 10000);

    public static PassportEsQuery getInstance() {
        return instance;
    }

    String getIndex(String indexName, long time) {
        return String.format("%s-%s", indexName, new SimpleDateFormat("yyyy.MM.dd").format(new Date(time)));
    }

    Map esAggregationSearch(String uri, long beginTime, String interval, String body) {

        PostMethod postMethod = new PostMethod(uri);
        postMethod.setRequestHeader("Content-Type", "application/json");
        postMethod.getParams().setContentCharset("utf-8");

        postMethod.setRequestEntity(new StringRequestEntity(body));
        try {
            String result = httpClientUtil.httpRequest(postMethod, "UTF-8");
            Map map = jsonMapper.fromJson(result, Map.class);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
            //todo send msg
        }
        return null;
    }

    /**
     * 查询beginTime之前5分钟
     * @param beginTime
     * @return
     */
    public Map<String, PassportEsResult> queryInternalPassport(long beginTime, String interval) {
        String uri = String.format(QUERY_FORMAT, ES_HOST, getIndex(INTERNAL_PASSPORT_INDEX, beginTime), INTERNAL_PASSPORT_TYPE);
        long endTime = beginTime / 1000 * 1000;
        long startTime = endTime - MIN_5_IN_MILLIS;
        return PassportEsResultConverter.convertAggregationResult(esAggregationSearch(uri, beginTime, interval, genInternalPassportBody(startTime,endTime,interval)));
    }

    /**
     * 查询plus.sohuno.com 业务线
     * @param beginTime
     * @param interval
     * @return
     */
    public Map<String, PassportEsResult> queryPlusSohunoAppKey(long beginTime, String interval) {
        String uri = String.format(QUERY_FORMAT, ES_HOST, getIndex(PLUS_SOHUNO_INDEX,beginTime), PLUS_SOHUNO_TYPE);
        long endTime = beginTime / 1000 * 1000;
        long startTime = endTime - MIN_5_IN_MILLIS;
        return PassportEsResultConverter.convertAggregationResult(esAggregationSearch(uri, beginTime, interval, genPlusSohunoAppIdBody(startTime,endTime,interval)));
    }


    String genInternalPassportBody(long startTime, long endTime, String interval) {
        return new StringBuilder(
                "{" +
                        "  \"query\": {" +
                        "    \"filtered\": {" +
                        "      \"query\": {" +
                        "        \"query_string\": {" +
                        "          \"analyze_wildcard\": true," +
                        "          \"query\": \"NOT \\\"/api/userinfo/get/*\\\"\"" +
                        "        }" +
                        "      }," +
                        "      \"filter\": {" +
                        "        \"bool\": {" +
                        "          \"must\": [" +
                        "            {" +
                        "              \"range\": {" +
                        "                \"@timestamp\": {" +
                        "                  \"gte\": " + startTime + "," +
                        "                  \"lte\": " + endTime + "," +
                        "                  \"format\": \"epoch_millis\"" +
                        "                }" +
                        "              }" +
                        "            }" +
                        "          ]," +
                        "          \"must_not\": [" +
                        "            {" +
                        "              \"query\": {" +
                        "                \"match\": {" +
                        "                  \"interface.raw\": {" +
                        "                    \"query\": \"/recv_contact.jsp\"," +
                        "                    \"type\": \"phrase\"" +
                        "                  }" +
                        "                }" +
                        "              }" +
                        "            }," +
                        "            {" +
                        "              \"query\": {" +
                        "                \"match\": {" +
                        "                  \"interface.raw\": {" +
                        "                    \"query\": \"/\"," +
                        "                    \"type\": \"phrase\"" +
                        "                  }" +
                        "                }" +
                        "              }" +
                        "            }" +
                        "          ]" +
                        "        }" +
                        "      }" +
                        "    }" +
                        "  }," +
                        "  \"size\": 0," +
                        "  \"aggs\": {" +
                        "    \"2\": {" +
                        "      \"date_histogram\": {" +
                        "        \"field\": \"@timestamp\"," +
                        "        \"interval\": \""+interval+"\"," +
                        "        \"time_zone\": \"Asia/Shanghai\"," +
                        "        \"min_doc_count\": 1," +
                        "        \"extended_bounds\": {" +
                        "          \"min\": " + startTime + "," +
                        "          \"max\": " + endTime + "" +
                        "        }" +
                        "      }," +
                        "      \"aggs\": {" +
                        "        \"3\": {" +
                        "          \"terms\": {" +
                        "            \"field\": \"interface.raw\"," +
                        "            \"size\": 30," +
                        "            \"order\": {" +
                        "              \"_count\": \"desc\"" +
                        "            }" +
                        "          }" +
                        "        }" +
                        "      }" +
                        "    }" +
                        "  }" +
                        "}").toString();
    }

    String genInternalPassportApiv2() {
        return "{" +
                "  \"query\": {" +
                "    \"filtered\": {" +
                "      \"query\": {" +
                "        \"query_string\": {" +
                "          \"query\": \"NOT \\\"/api/userinfo/get/*\\\"\"," +
                "          \"analyze_wildcard\": true" +
                "        }" +
                "      }," +
                "      \"filter\": {" +
                "        \"bool\": {" +
                "          \"must\": [" +
                "            {" +
                "              \"range\": {" +
                "                \"@timestamp\": {" +
                "                  \"gte\": 1487586873321," +
                "                  \"lte\": 1487587773321," +
                "                  \"format\": \"epoch_millis\"" +
                "                }" +
                "              }" +
                "            }" +
                "          ]," +
                "          \"must_not\": []" +
                "        }" +
                "      }" +
                "    }" +
                "  }," +
                "  \"size\": 0," +
                "  \"aggs\": {" +
                "    \"2\": {" +
                "      \"date_histogram\": {" +
                "        \"field\": \"@timestamp\"," +
                "        \"interval\": \"1m\"," +
                "        \"time_zone\": \"Asia/Shanghai\"," +
                "        \"min_doc_count\": 1," +
                "        \"extended_bounds\": {" +
                "          \"min\": 1487586873321," +
                "          \"max\": 1487587773321" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}";
    }

    String genPlusSohunoAppIdBody(long startTime, long endTime, String interval) {
        return "{" +
                "  \"size\": 0," +
                "  \"query\": {" +
                "    \"filtered\": {" +
                "      \"query\": {" +
                "        \"query_string\": {" +
                "          \"query\": \"*\"," +
                "          \"analyze_wildcard\": true" +
                "        }" +
                "      }," +
                "      \"filter\": {" +
                "        \"bool\": {" +
                "          \"must\": [" +
                "            {" +
                "              \"range\": {" +
                "                \"@timestamp\": {" +
                "                  \"gte\": "+startTime+"," +
                "                  \"lte\": "+endTime+"," +
                "                  \"format\": \"epoch_millis\"" +
                "                }" +
                "              }" +
                "            }" +
                "          ]," +
                "          \"must_not\": []" +
                "        }" +
                "      }" +
                "    }" +
                "  }," +
                "  \"aggs\": {" +
                "    \"2\": {" +
                "      \"date_histogram\": {" +
                "        \"field\": \"@timestamp\"," +
                "        \"interval\": \""+interval+"\"," +
                "        \"time_zone\": \"Asia/Shanghai\"," +
                "        \"min_doc_count\": 1," +
                "        \"extended_bounds\": {" +
                "          \"min\": "+startTime+"," +
                "          \"max\": "+endTime+"" +
                "        }" +
                "      }," +
                "      \"aggs\": {" +
                "        \"3\": {" +
                "          \"terms\": {" +
                "            \"field\": \"appkey.raw\"," +
                "            \"size\": 30," +
                "            \"order\": {" +
                "              \"_count\": \"desc\"" +
                "            }" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}";
    }
}
