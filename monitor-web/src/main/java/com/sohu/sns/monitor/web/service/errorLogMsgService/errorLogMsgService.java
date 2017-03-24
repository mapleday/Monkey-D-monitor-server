package com.sohu.sns.monitor.web.service.errorLogMsgService;

/**
 * Created by yw on 2017/3/23.
 */
public class errorLogMsgService {
    private int errThreshold;
    private String errMsgInfo;

    public String errMsgInfo(int appIdErrNums){
        int result=appIdErrNums-errThreshold;
        if (result>500){
            errMsgInfo="[严重] 今天 %s 错误已超过阀值 "+result+"次";
        }
        else if (appIdErrNums-errThreshold>250){
            errMsgInfo="[中级] 今天 %s 错误已超过阀值 "+result+"次";
        }
        return errMsgInfo;
    }

    public void  sendMsg(){
        
    }
}
