package com.sohu.sns.monitor.es.esresult;

import com.sohu.sns.monitor.es.module.PassportEsResult;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by morgan on 2017/2/20.
 */
public class PassportEsResultConverter {

    public static Map<String, PassportEsResult> convertAggregationResult(Map map) {
        Map<String, PassportEsResult> result = new HashMap<String, PassportEsResult>();
        Map hisMap = (Map) map.get("hits");
        if (hisMap == null || ((Integer) hisMap.get("total")) <= 0) {
            return result;
        }

        Map aggregationsMap = (Map) map.get("aggregations");
        Map l2Map = (Map) aggregationsMap.get("2");
        List<Map> l2List = (List<Map>) l2Map.get("buckets");
        for (Map l2 : l2List) {
            Map l3Map = (Map) l2.get("3");
            List<Map> l3List = (List<Map>) l3Map.get("buckets");
            for (Map keyMap : l3List) {
                PassportEsResult passportEsResult = new PassportEsResult();
                passportEsResult.setCount((Integer) keyMap.get("doc_count"));
                String key = keyMap.get("key").toString();
                passportEsResult.setInterfaceUri(key);
                passportEsResult.setTotalCount((Integer) l2.get("doc_count"));
                Date time = new Date((Long) l2.get("key"));
                passportEsResult.setTimeKey(new SimpleDateFormat("MM-dd HH:mm").format(time));
                String minKey = new SimpleDateFormat("HH:mm").format(time);
                passportEsResult.setMinKey(minKey);
                result.put(key+minKey, passportEsResult);
            }
        }
        return result;
    }



}
