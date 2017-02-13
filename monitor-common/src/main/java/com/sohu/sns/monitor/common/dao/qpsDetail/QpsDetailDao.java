package com.sohu.sns.monitor.common.dao.qpsDetail;

import com.sohu.sns.monitor.common.module.EsResult;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QpsDetailDao {
    //查询qps信息
    public List<EsResult> getQpsDetail();
    //更新qps信息
    public void updateEsResult(EsResult esResult);

}
