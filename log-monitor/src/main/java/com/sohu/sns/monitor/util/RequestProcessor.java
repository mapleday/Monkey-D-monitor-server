package com.sohu.sns.monitor.util;

import com.sohu.sns.monitor.constant.RequestValue;
import com.sohu.sns.monitor.constant.StatusConstant;
import com.sohu.sns.monitor.controller.ApiController;
import com.sohu.sns.monitor.controller.annotation.RequestParams;
import com.sohu.sns.monitor.global.MonitorRequest;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestProcessor {

	public static String monitor_controller_package = "com.sohu.sns.monitor.controller.ApiController";
	private final static Logger LOG = LoggerFactory.getLogger(RequestProcessor.class); 
    private final static Map<String, MonitorRequest> monitorRequestMap = new HashMap<String, MonitorRequest>();
    private static ApiController controllerInstance = null;

    /**
     * 初始化，缓存ApiController方法和方法注解参数
     */
    public static void init() {
        try {
            Class<ApiController> cls = (Class<ApiController>) Class.forName(monitor_controller_package);
            Method[] methods = cls.getMethods();
            for (Method method : methods) {
                RequestParams req = method.getAnnotation(RequestParams.class);
                if(req != null){
                    String path = req.path();
                    MonitorRequest monitorRequest = new MonitorRequest(path.toLowerCase(),
                            req.method(), req.required(), method,req.isCheckToken(),req.isCheckReplay());
                    System.out.println("...init MonitorController methods " + monitorRequest.toString());
                    LOG.info("...init MonitorController methods " + monitorRequest.toString());
                    monitorRequestMap.put(path.toLowerCase(), monitorRequest);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

	public static String wrap(int systemStatus, JSONObject jsonService) {
	 
		JSONObject jsonResponse = new JSONObject();
		int serviceStatus = StatusConstant.SUCCESS;
		JSONObject jsonData = null;
		if (jsonService != null) {
			if (jsonService.containsKey("status")) {
				serviceStatus = jsonService.getInt("status");
				if (serviceStatus == StatusConstant.SUCCESS && jsonService.containsKey("data")) {
					jsonData = jsonService.getJSONObject("data");
				}
			}
		}
		int status = serviceStatus != StatusConstant.SUCCESS ? serviceStatus : systemStatus;
		jsonResponse.put("status", status);
		StringBuilder desc = new StringBuilder();
		desc.append(StatusConstant.MAP_STATUS.get(status));
		if (jsonService != null && jsonService.containsKey("desc")) {
			String d = jsonService.getString("desc");
			if (d != null && !d.trim().toLowerCase().equals("")) {
				desc.append(" --> " + d);
			}
		}
		jsonResponse.put("desc", desc.toString());
		jsonResponse.put("data", jsonData); 
		return jsonResponse.toString();
	}

	public static JSONObject createJSONResult(String status, String desc, Object o) {
		JSONObject json = new JSONObject();
		json.put("status", status);
		json.put("desc", desc);
		if (o != null) {
			json.put("data", o);
		}
		return json;
	}

	public static String processHttpRequest(String path, Map<String, RequestValue> mapParams, String httpMethod) {

		JSONObject jsonParams = new JSONObject();
		for (String key : mapParams.keySet()) {
			jsonParams.put(key, mapParams.get(key).value.toString());
		}

		try {
			path = path.toLowerCase();
			MonitorRequest monitorRequest = monitorRequestMap.get(path);
			//优先从初始化好的map中获取对应的方法
			if (monitorRequest != null) {
				try {
					boolean isLegalRequestMethod = monitorRequest.getMethodSet().contains(httpMethod);
					if (!isLegalRequestMethod) {
						if (httpMethod.toLowerCase().equals("get")) {
							return StatusUtil.format(200005);
						}
						if (httpMethod.toLowerCase().equals("post")) {
							return StatusUtil.format(200006);
						}
					}
					if (monitorRequest.getRequiredSet().size() > 0) {
						String absenceParamsAll = absenceParamsRequiredAll(monitorRequest.getRequiredSet(), mapParams);
						if (absenceParamsAll.length() > 0) {
							return StatusUtil.format(200003, absenceParamsAll.toString(), null);
						}

						Method callMethod = monitorRequest.getMethod();
						ApiController apiController = getApiController();
						String result = (String) callMethod.invoke(apiController, mapParams);
						return result;
					}
				} catch (Exception e) {
					LOG.error("service invoke error", e);
					return StatusUtil.format(200001);
				}
			} else {
				try {
					@SuppressWarnings("unchecked")
					Class<ApiController> cls = (Class<ApiController>) Class.forName(monitor_controller_package);

					Method[] methods = cls.getMethods();
					for (Method method : methods) {
						Annotation[] annotations = method.getAnnotations();
						for (Annotation ann : annotations) {
							RequestParams req = ((RequestParams) ann);
							String[] requiredMethods = req.method();
							boolean isLegalRequestMethod = isRequiredMethod(requiredMethods, httpMethod);
							if (!isLegalRequestMethod) {
								if (httpMethod.toLowerCase().equals("get")) {
									return StatusUtil.format(200005);
								}
								if (httpMethod.toLowerCase().equals("post")) {
									return StatusUtil.format(200006);
								}
							}
							if (req.path().equals(path)) {
								if (req.required().length > 0) {
									String absenceParamsAll = absenceParamsRequiredAll(req.required(), mapParams);
									if (absenceParamsAll.length() > 0) {
										return StatusUtil.format(200003, absenceParamsAll.toString(), null);
									}
								}

								Method callMethod = monitorRequest.getMethod();
								ApiController apiController = getApiController();
								String result = (String) callMethod.invoke(apiController, mapParams);
								return result;

							}
						}
					}
				} catch (Exception ex) {
					LOG.error("service invoke error", ex);
					return StatusUtil.format(200001);
				}
				return StatusUtil.format(200002);
			}
		} catch (Exception e) {
			LOG.error("api error", e);
			return StatusUtil.format(200001);
		}
		return null;
	}

    private static ApiController getApiController() {
        if(controllerInstance == null)
            controllerInstance = SpringContextUtil.getBean(ApiController.class);
        return controllerInstance;
    }

    public static boolean isRequiredMethod(String[] requiredMethods, String m) {
		for(int i=0;i<requiredMethods.length;i++){
			if(requiredMethods[i].equals(m)){
				return true;
			}
		}
		return false;
	}

	public static String absenceParamsRequiredAll(String[] requireds, Map<String, RequestValue> mapParams) {
		StringBuilder absenceParams = new StringBuilder();
		for (int i = 0; i < requireds.length; i++) {
			String param = requireds[i];
			boolean isNull = !mapParams.containsKey(param);
			boolean isBlank = false;
			if(!isNull){
				RequestValue rv = mapParams.get(param);
				isBlank = rv.value == null || rv.value.toString().trim().equals("");
			}
			if (isNull || isBlank) {
				if (absenceParams.length() > 0) {
					absenceParams.append(",");
				}
				absenceParams.append(param);
			}
		}
		return absenceParams.toString();
	}

    public static String absenceParamsRequiredAll(Set<String> requireds, Map<String, RequestValue> mapParams) {
		StringBuilder absenceParams = new StringBuilder();
        for (String param : requireds) {
            boolean isNull = !mapParams.containsKey(param);
            boolean isBlank = false;
            if(!isNull){
                RequestValue rv = mapParams.get(param);
                isBlank = rv.value == null || rv.value.toString().trim().equals("");
            }
            if (isNull || isBlank) {
                if (absenceParams.length() > 0) {
                    absenceParams.append(",");
                }
                absenceParams.append(param);
            }
        }
		return absenceParams.toString();
	}


	public static boolean isOneOfParamsRequiredContained(String[] requireds, Map<String, RequestValue> mapParams) {
		for (int i = 0; i < requireds.length; i++) {
			String param = requireds[i];
			if (mapParams.containsKey(param)) {
				return true;
			}
		}
		return false;
	}

}
