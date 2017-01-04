package com.sohu.sns.monitor.log.enums;

/**
 * Created by chenshouqin on 2015/6/21.
 */
public enum ErrorLogFields {

    APP_ID(1, "appId"), //appId
    MODULE(2, "module"),   //模块名称
    METHOD(3, "method"),   //方法名称
    PARAM(4, "param"),  //参数
    RETURN_VALUE(5, "returnValue"), //返回值
    EXCEPTION_NAME(6, "exceptionName"), //异常名称
    EXCEPTION_DESC(7, "exceptionDesc"),    //异常描述
    STACK_TRACE(8, "stackTrace");   //

    private int type;
    private String name;

    private ErrorLogFields(int type, String name){
        this.type = type;
        this.name = name;
    }

    public static ErrorLogFields typeof(int type){
        switch (type){
            case 1 : return APP_ID;
            case 2 : return MODULE;
            case 3 : return METHOD;
            case 4 : return PARAM;
            case 5 : return RETURN_VALUE;
            case 6 : return EXCEPTION_NAME;
            case 7 : return EXCEPTION_DESC;
            case 8 : return STACK_TRACE;
        }
        return null;
    }

    public int getType() {
        return type;
    }
    public String getName() {
        return name;
    }
}
