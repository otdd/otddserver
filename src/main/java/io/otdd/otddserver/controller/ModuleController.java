package io.otdd.otddserver.controller;

import com.google.gson.Gson;
import io.otdd.otddserver.entity.Module;
import io.otdd.otddserver.entity.PageBean;
import io.otdd.otddserver.service.ModuleService;
import io.otdd.otddserver.vo.ModuleListQueryVo;
import io.otdd.otddserver.vo.ModuleVo;
import io.otdd.otddserver.vo.PageBeanVo;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@EnableAutoConfiguration
@RequestMapping("/otdd/module")
public class ModuleController extends BaseController{

	public static final Logger LOGGER = LoggerFactory.getLogger(ModuleController.class);

	@Autowired
	private ModuleService moduleService;

	@RequestMapping(value = "/deleteModule")
	@ResponseBody
	public Map<String, Object> deleteModule(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		int moduleId = json.getInt("id");
		if(moduleService.deleteModule(moduleId)){
			return success();
		}
		else{
			return fail();
		}
	}
	
//	@RequestMapping(value = "/newModule")
//	@ResponseBody
//	public Map<String, Object> newModule(@RequestBody String body) {
//		Gson gson = new Gson();
//		ModuleVo vo = gson.fromJson(body, ModuleVo.class);
//		Module module = changeModule(vo);
//		if(moduleService.createModule(module)){
//			Map<String,Object> ret = new HashMap<String,Object>();
//			ret.put("id", module.getId());
//			return success(ret);
//		}
//		else{
//			return fail();
//		}
//	}

	private Module changeModule(ModuleVo vo) {
		Module module = new Module();
		module.setName(vo.getName());
		module.setProtocol(vo.getProtocol());

//
//		Map<String, Map<String,Map<String,String>>> settings = new HashMap<String, Map<String,Map<String,String>>>();
//		for(PluginSettingsVo s:vo.getPluginConf()){
//			Map<String,Map<String,String>> configMap = new HashMap<String,Map<String,String>>();
//			List<PluginSettingVo> req = s.getRequest();
//			if(req!=null&&req.size()>0){
//				Map<String,String> tmp = new HashMap<String,String>();
//				for(PluginSettingVo ss:req){
//					tmp.put(ss.getName(),ss.getCurrentValue());
//				}
//				configMap.put("request", tmp);
//			}
//			List<PluginSettingVo> resp = s.getResponse();
//			if(resp!=null&&resp.size()>0){
//				Map<String,String> tmp = new HashMap<String,String>();
//				for(PluginSettingVo ss:resp){
//					tmp.put(ss.getName(),ss.getCurrentValue());
//				}
//				configMap.put("response", tmp);
//			}
//			if(configMap.size()>0){
//				settings.put(s.getName(), configMap);
//			}
//		}
//		Gson gson = new Gson();
//		module.setPluginConf(gson.toJson(settings));
		return module;
	}

	@RequestMapping(value = "/getModule")
	@ResponseBody
	public Map<String, Object> getModule(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		int moduleId = json.getInt("id");
		Module module = moduleService.getModule(moduleId);
		if(module!=null){
			ModuleVo ret = new ModuleVo(module);
			return success(ret);
		}
		else{
			return fail();
		}
	}

	@RequestMapping(value = "/getModuleList")
	@ResponseBody
	public Map<String, Object> getModuleList(@RequestBody String body) {
		Gson gson = new Gson();
		ModuleListQueryVo vo = gson.fromJson(body, ModuleListQueryVo.class);
		PageBean<Module> list = moduleService.getModuleList(vo.getPageInfo().getCurrent(),vo.getPageInfo().getPageSize());
		if(list!=null){
			PageBeanVo ret = new PageBeanVo(list);
			ret.setData(list.getData());
			return success(ret);
		}
		else{
			return fail();
		}
	}

}
