package com.sohu.sns.monitor.es.query;

import com.sohu.sns.monitor.es.module.PassportEsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by morgan on 2017/2/20.
 */
public class PassportEsAnalysis {

    static final PassportEsAnalysis instance = new PassportEsAnalysis();
    //1分钟时间间隔聚合
    final String MINUTE_INTERVAL = "1m";
    //1秒钟时间间隔聚合
    final String SECOND_INTERVAL = "1s";

    final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;

    final Float compareLevel[] = new Float[]{ 10.0f, 5.0f, 3.5f, 2.5f, 1.5f };

    public static PassportEsAnalysis getInstance() {
        return instance;
    }

    public List<PassportEsResult> analysisTowDayQpm(float alertRate) {
        long start = System.currentTimeMillis();
        Map<String, PassportEsResult> todayResult = PassportEsQuery.getInstance().queryInternalPassport(start, MINUTE_INTERVAL);
        Map<String, PassportEsResult> yesTodayResult = PassportEsQuery.getInstance().queryInternalPassport(start - ONE_DAY_MILLIS, MINUTE_INTERVAL);
        return analysisTowDay(todayResult, yesTodayResult, alertRate);
    }

    public List<PassportEsResult> analysisTowDayAppKey(float alertRate) {
        long start = System.currentTimeMillis();
        Map<String, PassportEsResult> todayResult = PassportEsQuery.getInstance().queryPlusSohunoAppKey(start, MINUTE_INTERVAL);
        Map<String, PassportEsResult> yesTodayResult = PassportEsQuery.getInstance().queryPlusSohunoAppKey(start - ONE_DAY_MILLIS, MINUTE_INTERVAL);
        return analysisTowDay(todayResult, yesTodayResult, alertRate);
    }

    private List<PassportEsResult> analysisTowDay(Map<String, PassportEsResult> todayResult,
                                                  Map<String, PassportEsResult> yesTodayResult,
                                                  float alertRate) {
        List<PassportEsResult> results = new ArrayList<PassportEsResult>();
        for (String minKey : todayResult.keySet()) {
            PassportEsResult today = todayResult.get(minKey);
            if (today.getCount() <= 60) {
                continue;
            }
            PassportEsResult yesToday = yesTodayResult.get(minKey);
//            System.out.println("analysis:"+today.getInterfaceUri()+ "," + today.getCount() + "," + yesToday.getCount());
            float rate = ((float) today.getCount()) / yesToday.getCount();
            if (rate >= alertRate) {
                today.setLastCount(yesToday.getCount());
                today.setLastTotalCount(yesToday.getTotalCount());
                results.add(today);
            }
        }
        return results;
    }


}
