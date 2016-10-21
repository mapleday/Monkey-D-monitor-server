package com.sohu.sns.monitor.util;

import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.constant.StatusConstant;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StatusUtil {

	static final int SUCCESS = 100000;

	static HashMap<Integer, String> mapStaus = StatusConstant.MAP_STATUS;
	public static String format(int status, String desc, JSONObject data){
		JSONObject json = new JSONObject();
		String dsc  =  mapStaus.get(status);
		if(dsc == null){
			desc = "unknown error";
		} else{
			if(desc != null){
				desc = dsc + " --> " + desc;
			} else if(desc == null){
				desc = dsc;
			}
		}
		
		json.put("status", status);
		json.put("desc", desc);
		json.put("data", data);
		return json.toString();
	}
	
	public static String format(JSONObject data){
		JSONObject json = new JSONObject();
		json.put("status", SUCCESS);
		json.put("desc", mapStaus.get(SUCCESS));
		json.put("data", data);
		return json.toString();
	}

    public static String formatMap(Map<String,Object> jsonData) {
        HashMap<String,Object> hashMap = new HashMap<String, Object>();
        hashMap.put("status",SUCCESS);
        hashMap.put("desc",mapStaus.get(SUCCESS));
        hashMap.put("data",jsonData);
        return JsonMapper.nonEmptyMapper().toJson(hashMap);
    }

	public static String format(int status){
		return format(status, "");
	}
	
	public static String format(int status, String desc){
		return format(status, desc, null);
	}
	 
}
