package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.es.module.ErrorLog;
import com.sohu.sns.monitor.es.query.SnsEsQuery;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yw on 2017/3/16.
 */
@Controller
public class ErrorLogsController {
    @RequestMapping("/errorLogs")
    public String errorLogs(){
        return "errorLogs";
    }

    @ResponseBody
    @RequestMapping("/getErrorLogs")
    public Map getErrorLogs() throws UnknownHostException {
        Map<String, Object> map = new HashMap<String, Object>();
        SnsEsQuery snsEsQuery = new SnsEsQuery();
        String indexName = "logstash_snsweb-2017.03.16";
        String type = "logs";
        QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("module", "sns_cc_dm"));
        List<ErrorLog> errorLogs = snsEsQuery.queryErrorLogs(queryBuilder, indexName, type);
        map.put("data",errorLogs);
        return map;


    }
}
