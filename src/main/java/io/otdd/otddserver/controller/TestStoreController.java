package io.otdd.otddserver.controller;

import io.otdd.ddl.plugin.DDLCodecFactory;
import io.otdd.otddserver.ddl.DdlService;
import io.otdd.otddserver.ddl.ParseToDDLResult;
import io.otdd.otddserver.entity.Module;
import io.otdd.otddserver.plugin.PluginMgr;
import io.otdd.otddserver.service.TestService;
import io.otdd.otddserver.service.ModuleService;
import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.vo.ParseToDDLResultVo;
import io.otdd.otddserver.vo.TestVo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@EnableAutoConfiguration
@RequestMapping("/otdd/teststore")
public class TestStoreController extends BaseController{

	public static final Logger LOGGER = LoggerFactory.getLogger(TestStoreController.class);

	@Autowired
    private ModuleService moduleService;
	
	@Autowired
    private TestService testService;
	
	@Autowired
	private DdlService ddlService;
	
	@RequestMapping(value = "/getTest")
	@ResponseBody
	public Map<String, Object> getTest(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String testId = json.getString("id");
		int moduleId = json.getInt("moduleId");
		Test test = testService.getTestById(moduleId,testId);
		if(test!=null){
			TestVo testVo = new TestVo(test);
			return success(testVo);
		}
		else{
			return fail();
		}
	}
	
}
