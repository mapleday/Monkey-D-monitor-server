package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.qpsDetail.QpsDetailDao;
import com.sohu.sns.monitor.common.module.EsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Administrator on 2017/1/20.
 */
@Component
public class QpsDetailService {

    @Autowired
    QpsDetailDao qpsDetailDao;

    public Set<EsResult> getQpsDetail(){
        Set<EsResult> orderResults = new TreeSet();
        List<EsResult> esResultList =  qpsDetailDao.getQpsDetail();
        for (EsResult esReuslt:esResultList) {
            orderResults.add(esReuslt);
        }
        return  orderResults;
    }

    public void updateDetail(EsResult esResult){

        this.qpsDetailDao.updateEsResult(esResult);
    }

}
