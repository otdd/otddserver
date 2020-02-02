package io.otdd.otddserver.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.otdd.ddl.plugin.DDLCodecFactory;
import io.otdd.otddserver.entity.PageBean;
import io.otdd.otddserver.entity.PluginInfo;
import io.otdd.otddserver.plugin.PluginMgr;
import io.otdd.otddserver.plugincenter.DeletePluginVo;
import io.otdd.otddserver.plugincenter.InstallPluginVo;
import io.otdd.otddserver.service.PluginService;
import io.otdd.otddserver.service.SysConfService;
import io.otdd.otddserver.vo.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Type;
import java.util.*;

@Controller
@EnableAutoConfiguration
@RequestMapping("/otdd/plugin")
public class PluginController extends BaseController{

	@Autowired
	private SysConfService sysConfService;

	@Autowired
	private PluginService pluginService;

	@RequestMapping(value = "/getPluginList")
	@ResponseBody
	public Map<String, Object> getPluginList(@RequestBody String body) {
		Set<String> plugins = PluginMgr.getInstance().getDDLCodecFactories().keySet();
		return success(plugins);
	}
	
	@RequestMapping(value = "/getAllPluginSettings")
	@ResponseBody
	public Map<String, Object> getAllPluginSettings() {
		List<PluginSettingsVo> ret = new ArrayList<PluginSettingsVo>();
		for(DDLCodecFactory factory : PluginMgr.getInstance().getDDLCodecFactories().values()){
			PluginSettingsVo tmp = new PluginSettingsVo();
			tmp.setPluginName(factory.getPluginName());
			tmp.setPluginVersion(factory.getPluginVersion());
			Map<String,String> reqSettings = factory.getReqProtocolSettings();
			if(reqSettings!=null){
				tmp.setRequest(convertPluginSettings(reqSettings));
			}
			Map<String,String> respSettings = factory.getRespProtocolSettings();
			if(respSettings!=null){
				tmp.setResponse(convertPluginSettings(respSettings));
			}
			ret.add(tmp);
		}
		return success(ret);
	}
	
	@RequestMapping(value = "/saveAllPluginSettings")
	@ResponseBody
	public Map<String, Object> saveAllPluginSettings(@RequestBody String body) {
		Gson gson = new Gson();
        Type listType = new TypeToken<List<PluginSettingsVo> > () { }.getType();
		List<PluginSettingsVo> allSettings = gson.fromJson(body, listType);
		if(allSettings==null||allSettings.size()==0){
			return success();
		}
		
		Map<String, Map<String,Map<String,String>>> sysPluginSettings = new HashMap<String, Map<String,Map<String,String>>>();
		for(PluginSettingsVo settings:allSettings){
			Map<String,Map<String,String>> configMap = new HashMap<String,Map<String,String>>();
			String pluginName = settings.getPluginName();
			DDLCodecFactory factory = PluginMgr.getInstance().getDDLCodecFactory(pluginName);
			if(factory==null){
				continue;
			}
			List<PluginSettingVo> req = settings.getRequest();
			Map<String,String> reqSettings = new HashMap<String,String>();
			for(PluginSettingVo s:req){
				reqSettings.put(s.getName(),s.getCurrentValue());
			}
			if(reqSettings.size()>0){
				configMap.put("request", reqSettings);
			}
			
			List<PluginSettingVo> resp = settings.getResponse();
			Map<String,String> respSettings = new HashMap<String,String>();
			for(PluginSettingVo s:resp){
				respSettings.put(s.getName(),s.getCurrentValue());
			}
			if(respSettings.size()>0){
				configMap.put("response", respSettings);
			}
			
			if(configMap.size()>0){
				sysPluginSettings.put(pluginName, configMap);
			}
			
			factory.updateSettings(reqSettings, respSettings);
		}
		
		if(sysConfService.saveConfigByKey("plugin_conf",gson.toJson(sysPluginSettings))){
			return success();
		}
		return fail();
	}
	
	@RequestMapping(value = "/getPluginSettings")
	@ResponseBody
	public Map<String, Object> getPluginSettings(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String pluginName = json.getString("pluginName");
		DDLCodecFactory factory = PluginMgr.getInstance().getDDLCodecFactory(pluginName);
		if(factory==null){
			return fail();
		}
		PluginSettingsVo ret = new PluginSettingsVo();
		ret.setPluginName(pluginName);
		ret.setPluginVersion(factory.getPluginVersion());
		Map<String,String> reqSettings = factory.getReqProtocolSettings();
		if(reqSettings!=null){
			ret.setRequest(convertPluginSettings(reqSettings));
		}
		Map<String,String> respSettings = factory.getRespProtocolSettings();
		if(respSettings!=null){
			ret.setResponse(convertPluginSettings(respSettings));
		}
		return success(ret);
	}

	@RequestMapping(value = "/getPluginListInStore")
	@ResponseBody
	public Map<String, Object> getPluginListInStore(@RequestBody String body) {
		Gson gson = new Gson();
		PageBeanVo vo = gson.fromJson(body, PageBeanVo.class);
		PageBean<PluginInfo> list = pluginService.getPluginListInStore(vo.getPageInfo().getCurrent(),vo.getPageInfo().getPageSize());
		if(list!=null){
			PageBeanVo ret = new PageBeanVo(list);
			ret.setData(list.getData());
			return success(ret);
		}
		return fail();
	}

	@RequestMapping(value = "/installPlugin")
	@ResponseBody
	public Map<String, Object> installPlugin(@RequestBody String body) {
		Gson gson = new Gson();
		InstallPluginVo vo = gson.fromJson(body, InstallPluginVo.class);
		if(pluginService.installPlugin(vo.getExistingPluginName(),vo.getDownloadUrl())){
			return success();
		}
		return fail();
	}

	@RequestMapping(value = "/deletePlugin")
	@ResponseBody
	public Map<String, Object> deletePlugin(@RequestBody String body) {
		Gson gson = new Gson();
		DeletePluginVo vo = gson.fromJson(body, DeletePluginVo.class);
		if(pluginService.deletePlugin(vo.getPluginName())){
			return success();
		}
		return fail();
	}
	

	private List<PluginSettingVo> convertPluginSettings(Map<String, String> settings) {
		List<PluginSettingVo> ret = new ArrayList<PluginSettingVo>();
		if(settings==null){
			return ret;
		}
		for(String name:settings.keySet()){
			PluginSettingVo setting = new PluginSettingVo();
			setting.setName(name);
			String tmp = settings.get(name);
			JSONObject tmpJson = JSONObject.fromObject(tmp);
			setting.setHint(tmpJson.optString("hint"));
			setting.setCurrentValue(tmpJson.optString("currentValue"));
			setting.setType(tmpJson.optString("type"));
			
			List<String> values = new ArrayList<String>();
			JSONArray array = tmpJson.optJSONArray("values");
			if(array!=null){
				for(int i=0;i<array.size();i++){
					values.add(array.getString(i));
				}
			}
			setting.setValues(values);
			ret.add(setting);
		}
		return ret;
	}
	
}
