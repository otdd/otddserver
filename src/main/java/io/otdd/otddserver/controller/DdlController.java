package io.otdd.otddserver.controller;

import io.otdd.ddl.plugin.DDLCodecFactory;
import io.otdd.otddserver.ddl.DdlService;
import io.otdd.otddserver.ddl.ParseFromDDLResult;
import io.otdd.otddserver.ddl.ParseToDDLResult;
import io.otdd.otddserver.edit.EditTest;
import io.otdd.otddserver.entity.Module;
import io.otdd.otddserver.plugin.PluginMgr;
import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.service.EditService;
import io.otdd.otddserver.service.ModuleService;
import io.otdd.otddserver.service.TestService;
import io.otdd.otddserver.testcase.TestBase;
import io.otdd.otddserver.vo.ParseFromDDLResultVo;
import io.otdd.otddserver.vo.ParseToDDLResultVo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
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
@RequestMapping("/otdd/ddl")
public class DdlController extends BaseController{

    public static final Logger LOGGER = LoggerFactory.getLogger(DdlController.class);

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private TestService testService;

    @Autowired
    private EditService editService;

    @Autowired
    private DdlService ddlService;

    @RequestMapping(value = "/parseInboundReqToDDL")
    @ResponseBody
    public Map<String, Object> parseInboundReqToDDL(@RequestBody String body) {
        JSONObject json = JSONObject.fromObject(body);
        String testId = json.getString("id");
        String testStoreType = json.getString("testStoreType");
        JSONObject settings = json.optJSONObject("pluginSettings");
        String pluginName = null;
        if(settings!=null){
            pluginName = settings.optString("pluginName");
        }
        Map<String,String> setting = convertSettings(settings);
        TestBase test;
        if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(testStoreType)){
            int moduleId = json.getInt("moduleId");
            test = testService.getTestById(moduleId,testId);
        }
        else{
            test = editService.getEditTestById(testId);
        }
        if(test!=null){
            if(StringUtils.isBlank(pluginName)){
                Module module = moduleService.getModule(test.getModuleId());
                DDLCodecFactory factory = PluginMgr.getInstance().getDDLCodecFactoryByProtocol(module.getProtocol());
                if(factory==null){
                    return fail();
                }
                pluginName = factory.getPluginName();
            }

            ParseToDDLResult result = ddlService.parseToDDL(
                    DdlService.ParseType.REQUEST,pluginName,setting,test.getInboundCall().getReqBytes());
            if(result!=null){
                ParseToDDLResultVo ret = new ParseToDDLResultVo(result);
                return success(ret);
            }
        }
        return fail();
    }

    @RequestMapping(value = "/parseInboundRespToDDL")
    @ResponseBody
    public Map<String, Object> parseInboundRespToDDL(@RequestBody String body) {
        JSONObject json = JSONObject.fromObject(body);
        String testId = json.getString("id");
        String testStoreType = json.getString("testStoreType");
        JSONObject settings = json.optJSONObject("pluginSettings");
        String pluginName = null;
        if(settings!=null){
            pluginName = settings.optString("pluginName");
        }
        Map<String,String> setting = convertSettings(settings);
        TestBase test;
        if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(testStoreType)){
            int moduleId = json.getInt("moduleId");
            test = testService.getTestById(moduleId,testId);
        }
        else{
            test = editService.getEditTestById(testId);
        }
        if(test!=null){
            if(StringUtils.isBlank(pluginName)){
                Module module = moduleService.getModule(test.getModuleId());
                DDLCodecFactory factory = PluginMgr.getInstance().getDDLCodecFactoryByProtocol(module.getProtocol());
                if(factory==null){
                    return fail();
                }
                pluginName = factory.getPluginName();
            }
            ParseToDDLResult result = ddlService.parseToDDL(
                    DdlService.ParseType.RESPONSE, pluginName,setting, test.getInboundCall().getRespBytes());
            if(result!=null){
                ParseToDDLResultVo ret = new ParseToDDLResultVo(result);
                return success(ret);
            }
        }
        return fail();
    }

    @RequestMapping(value = "/parseOutboundReqToDDL")
    @ResponseBody
    public Map<String, Object> parseOutboundReqToDDL(@RequestBody String body) {
        JSONObject json = JSONObject.fromObject(body);
        String testId = json.getString("id");
        String testStoreType = json.getString("testStoreType");
        TestBase test;
        if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(testStoreType)){
            int moduleId = json.getInt("moduleId");
            test = testService.getTestById(moduleId,testId);
        }
        else{
            test = editService.getEditTestById(testId);
        }
        int index = json.getInt("index");
        JSONObject settings = json.optJSONObject("pluginSettings");
        String pluginName = null;
        if(settings!=null){
            pluginName = settings.optString("pluginName");
        }
        Map<String,String> setting = convertSettings(settings);
        if(test!=null&&test.getOutboundCalls().size()>index){
            ParseToDDLResult result = ddlService.parseToDDL(
                    DdlService.ParseType.REQUEST, pluginName, settings,test.getOutboundCalls().get(index).getReqBytes());
            if(result!=null){
                ParseToDDLResultVo ret = new ParseToDDLResultVo(result);
                return success(ret);
            }
        }
        return fail();
    }

    @RequestMapping(value = "/parseOutboundRespToDDL")
    @ResponseBody
    public Map<String, Object> parseOutboundRespToDDL(@RequestBody String body) {
        JSONObject json = JSONObject.fromObject(body);
        String testId = json.getString("id");
        String testStoreType = json.getString("testStoreType");
        TestBase test;
        if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(testStoreType)){
            int moduleId = json.getInt("moduleId");
            test = testService.getTestById(moduleId,testId);
        }
        else{
            test = editService.getEditTestById(testId);
        }
        int index = json.getInt("index");
        JSONObject settings = json.optJSONObject("pluginSettings");
        String pluginName = null;
        if(settings!=null){
            pluginName = settings.optString("pluginName");
        }
        Map<String,String> setting = convertSettings(settings);

        if(test!=null&&test.getOutboundCalls().size()>index){
            ParseToDDLResult result = ddlService.parseToDDL(
                    DdlService.ParseType.RESPONSE, pluginName, setting,test.getOutboundCalls().get(index).getRespBytes());
            if(result!=null){
                ParseToDDLResultVo ret = new ParseToDDLResultVo(result);
                return success(ret);
            }
        }
        return fail();
    }

    @RequestMapping(value = "/parseReqFromDDL")
    @ResponseBody
    public Map<String, Object> parseReqFromDDL(@RequestBody String body) {
        JSONObject json = JSONObject.fromObject(body);
        String ddl = json.getString("ddl");

        JSONObject settingsJson = json.optJSONObject("pluginSettings");
        String pluginName = null;
        if(settingsJson!=null){
            pluginName = settingsJson.optString("pluginName");
        }
        Map<String,String> setting = convertSettings(settingsJson);
        ParseFromDDLResult result = ddlService.parseFromDDL(
                DdlService.ParseType.REQUEST, pluginName,setting, ddl);
        if(result!=null&&result.getErrCode()==ParseFromDDLResult.Code.SUCCEED){
            ParseFromDDLResultVo ret = new ParseFromDDLResultVo(result);
            return success(ret);
        }
        else{
            return fail();
        }
    }

    @RequestMapping(value = "/parseRespFromDDL")
    @ResponseBody
    public Map<String, Object> parseRespFromDDL(@RequestBody String body) {
        JSONObject json = JSONObject.fromObject(body);
        String ddl = json.getString("ddl");
        JSONObject settingsJson = json.optJSONObject("pluginSettings");
        String pluginName = null;
        if(settingsJson!=null){
            pluginName = settingsJson.optString("pluginName");
        }
        Map<String,String> setting = convertSettings(settingsJson);

        ParseFromDDLResult result = ddlService.parseFromDDL(
                DdlService.ParseType.RESPONSE, pluginName, setting,ddl);
        if(result!=null&&result.getErrCode()==ParseFromDDLResult.Code.SUCCEED){
            ParseFromDDLResultVo ret = new ParseFromDDLResultVo(result);
            return success(ret);
        }
        else{
            return fail();
        }
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
