package com.sohu.sns.monitor.web.controler;


import com.sohu.sns.monitor.common.module.EsResult;
import com.sohu.sns.monitor.common.services.QpsDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Set;

/**
 * Created by lc on 2017/1/20.
 */
@Controller
public class QpsDetailController {

    @Autowired
    QpsDetailService qpsDetailService;

    @RequestMapping("/detail")
    @ResponseBody
    public String detail(Model model) {
        Set<EsResult> detailSet = qpsDetailService.getQpsDetail();
        model.addAttribute("detailList",detailSet);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"UTF-8\"><title>monitor</title></head><body>" +
                "<table border=\"1\" ><tr><td>  interfaceUri  </td><td>  avgTime  </td><td>  totoalCount  </td><td>  qps  </td><td>  updateTime  </td></tr>");
        for (EsResult result:detailSet) {
            String interfaceUri = result.getInterfaceUri();
            double avgtime = result.getAvgTime();
            double totalcount = result.getTotoalCount();
            double qps = result.getQps();
            String updateTime = result.getUpdateTime();
            sb.append("<tr><td>").append(interfaceUri).append("</td><td>").append(avgtime).append("</td><td>").append(totalcount).append("</td><td>").append(qps).append("</td><td>").append(updateTime).append("</td></tr>");
        }
        sb.append("</table></body></html>");

        return sb.toString();
    }




}
