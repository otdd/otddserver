package io.otdd.otddserver.controller;

import java.util.HashMap;
import java.util.Map;

public class BaseController {
	protected Map<String, Object> success(){
		return success(null);
	}
	protected Map<String, Object> success(Object data){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", true);
		if(data!=null){
			map.put("data", data);
		}
		return map;
	}
	protected Map<String, Object> fail(){
		return fail("failed");
	}
	protected Map<String, Object> fail(String errMsg){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", false);
		map.put("data", errMsg);
		return map;
	}
}
