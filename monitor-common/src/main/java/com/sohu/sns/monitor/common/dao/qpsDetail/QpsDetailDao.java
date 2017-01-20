package com.sohu.sns.monitor.common.dao.qpsDetail;

import com.sohu.sns.monitor.common.module.EsResult;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QpsDetailDao {
    public List<EsResult> getQpsDetail();
    public void updateEsResult(EsResult esResult);

}
