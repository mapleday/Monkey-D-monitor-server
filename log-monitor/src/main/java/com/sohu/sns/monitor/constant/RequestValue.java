package com.sohu.sns.monitor.constant;

public class RequestValue {
	public enum ReqestParamsType {
		STRING, STRING_ARRAY, BYTEARRAY,MAP
	}
	
	public ReqestParamsType type;
	
	public Object value;
	
	public RequestValue(ReqestParamsType type, Object value) {
		this.type = type;
		this.value = value;
	}

    @Override
    public String toString() {
        return value.toString();
    }
}
