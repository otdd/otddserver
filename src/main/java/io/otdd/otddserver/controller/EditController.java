package io.otdd.otddserver.controller;

import io.otdd.otddserver.ddl.DdlService;
import io.otdd.otddserver.ddl.ParseFromDDLResult;
import io.otdd.otddserver.match.MatchType;
import io.otdd.otddserver.service.EditService;
import io.otdd.otddserver.service.ModuleService;
import io.otdd.otddserver.util.EsTextUtil;
import io.otdd.otddserver.edit.EditTest;
import io.otdd.otddserver.edit.EditOutboundCall;
import io.otdd.otddserver.vo.EditTestVo;
import io.otdd.otddserver.vo.EditOutboundVo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@EnableAutoConfiguration
@RequestMapping("/otdd/edit")
public class EditController extends BaseController{

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private DdlService ddlService;

	@Autowired
	private EditService editService;

	@RequestMapping(value = "/newEdit")
	@ResponseBody
	public Map<String, Object> newEdit(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String testId = json.getString("testId");
		int moduleId = json.getInt("moduleId");
		String editTestId = editService.newEdit(moduleId,testId);
		if(!StringUtils.isEmpty(editTestId)){
			return success(editTestId);
		}
		return fail();
	}

	@RequestMapping(value = "/getEditTest")
	@ResponseBody
	public Map<String, Object> getEditTest(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String testId = json.getString("id");
		EditTest editTest = editService.getEditTestById(testId);
		if(editTest!=null){
			EditTestVo editTestVo = new EditTestVo(editTest);
			return success(editTestVo);
		}
		else{
			return fail();
		}
	}

	@RequestMapping(value = "/saveOutboundReq")
	@ResponseBody
	public Map<String, Object> saveOutboundReq(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String testId = json.getString("id");
		int index = json.getInt("index");
		int matchType = json.getInt("matchType");
		String content = json.getString("content");

		EditTest editTest = editService.getEditTestById(testId);
		if(editTest.getOutboundCalls().size()>index){
			EditOutboundCall call = (EditOutboundCall)editTest.getOutboundCalls().get(index);
			call.setMatchType(MatchType.values()[matchType]);
			if(!StringUtils.isEmpty(content)){
				call.setReqBytes(content.getBytes());
				call.setReqText(EsTextUtil.getTextFromBytes(call.getReqBytes()));
			}
			if(editService.updateEditTest(editTest)){
				return success();
			}
		}
		return fail();
	}
	
	@RequestMapping(value = "/saveOutboundResp")
	@ResponseBody
	public Map<String, Object> saveOutboundResp(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String testId = json.getString("id");
		int index = json.getInt("index");
		String ddl = json.getString("ddl");
		JSONObject settingsJson = json.optJSONObject("pluginSettings");
		String pluginName = null;
		if(settingsJson!=null){
			pluginName = settingsJson.optString("pluginName");
		}
		Map<String,String> setting = convertSettings(settingsJson);
		
		EditTest editTest = editService.getEditTestById(testId);
		if(editTest.getOutboundCalls().size()>index){
			EditOutboundCall call = (EditOutboundCall)editTest.getOutboundCalls().get(index);
			
			ParseFromDDLResult result = ddlService.parseFromDDL(
					DdlService.ParseType.RESPONSE, pluginName, setting,ddl);
			if(result!=null&&result.getErrCode()==ParseFromDDLResult.Code.SUCCEED){
				call.setRespBytes(result.getBytes());
				call.setRespText(EsTextUtil.getTextFromBytes(call.getRespBytes()));
				if(editService.updateEditTest(editTest)){
					return success();
				}
			}
			
		}
		return fail();
	}

	@RequestMapping(value = "/saveInboundReq")
	@ResponseBody
	public Map<String, Object> saveInboundReq(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String testId = json.getString("id");
		String ddl = json.getString("ddl");
		JSONObject settingsJson = json.optJSONObject("pluginSettings");
		String pluginName = null;
		if(settingsJson!=null){
			pluginName = settingsJson.optString("pluginName");
		}
		Map<String,String> setting = convertSettings(settingsJson);
		
		EditTest editTest = editService.getEditTestById(testId);
		if(editTest!=null){
			ParseFromDDLResult result = ddlService.parseFromDDL(
					DdlService.ParseType.REQUEST, pluginName,setting, ddl);
			if(result!=null&&result.getErrCode()==ParseFromDDLResult.Code.SUCCEED){
				editTest.getInboundCall().setReqBytes(result.getBytes());
				if(editService.updateEditTest(editTest)){
					return success();
				}
			}
		}
		return fail();
	}
	
	@RequestMapping(value = "/deleteOutbound")
	@ResponseBody
	public Map<String, Object> deleteOutbound(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String testId = json.getString("id");
		int index = json.getInt("index");
		EditTest editTest = editService.getEditTestById(testId);
		if(editTest!=null&&editTest.getOutboundCalls().size()>index){
			editTest.getOutboundCalls().remove(index);
			if(editService.updateEditTest(editTest)){
				return success();
			}
		}
		return fail();
	}

	@RequestMapping(value = "/addOutbound")
	@ResponseBody
	public Map<String, Object> addOutbound(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String testId = json.getString("id");
		int index = json.getInt("index");
		EditTest editTest = editService.getEditTestById(testId);
		if(editTest!=null&&editTest.getOutboundCalls().size()>=index){
			EditOutboundCall call = new EditOutboundCall();
			call.setReqBytes("".getBytes());
			call.setRespBytes("".getBytes());
			editTest.getOutboundCalls().add(index, call);
			if(editService.updateEditTest(editTest)){
				return success(new EditOutboundVo(call));
			}
		}
		return fail();
	}
	
	private Map<String,String> convertSettings(JSONObject settings){
		Map<String,String> setting = new HashMap<String,String>();
		if(settings!=null){
			JSONArray array = settings.optJSONArray("settings");
			if(array!=null){
				for(int i=0;i<array.size();i++){
					JSONObject tmp = array.getJSONObject(i);
					String name = tmp.optString("name");
					String value = tmp.optString("currentValue");
					setting.put(name, value);
				}
			}
		}
		return setting;
	}
}
