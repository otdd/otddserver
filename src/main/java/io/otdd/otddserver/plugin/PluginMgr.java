package io.otdd.otddserver.plugin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.otdd.ddl.plugin.DDLCodecFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PluginMgr {

	private static final Logger LOGGER = LogManager.getLogger();

	private Map<String,DDLCodecFactory> ddlFactories = new TreeMap<String,DDLCodecFactory>();
	
	private static PluginMgr instance;
	private PluginManager pluginManager;
	
	public static PluginMgr getInstance(){
		if(instance==null){
			instance = new PluginMgr();
		}
		return instance;
	}

	public void init(String pluginPath,String settings){
		Path path = new File(pluginPath).toPath();
		pluginManager = new DefaultPluginManager(path);
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        
        Gson gson = new Gson();
        Type listType = new TypeToken<Map<String, Map<String,Map<String,String> > > > () { }.getType();
        Map<String, Map<String,Map<String,String> > > tmp = gson.fromJson(settings, listType);
        this.initPlugins(tmp);
	}

	public void reInit(String pluginPath,String settings){
		try {
			pluginManager.stopPlugins();
		}
		catch (Exception e){
			e.printStackTrace();
		}

		for(DDLCodecFactory factory:ddlFactories.values()){
			try{
				factory.destroy();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}

		ddlFactories = new TreeMap<String,DDLCodecFactory>();

		Path path = new File(pluginPath).toPath();
		pluginManager = new DefaultPluginManager(path);
		pluginManager.loadPlugins();
		pluginManager.startPlugins();

		Gson gson = new Gson();
		Type listType = new TypeToken<Map<String, Map<String,Map<String,String> > > > () { }.getType();
		Map<String, Map<String,Map<String,String> > > tmp = gson.fromJson(settings, listType);
		this.initPlugins(tmp);
	}
	
	public Map<String,DDLCodecFactory> getDDLCodecFactories(){
		return ddlFactories;
	}
	
	public DDLCodecFactory getDDLCodecFactory(String pluginName){
		return ddlFactories.get(pluginName.toLowerCase());
	}
	
	private int initPlugins(Map<String, Map<String,Map<String,String>>> settings){
		
		int cnt = 0;
		
		List<DDLCodecFactory> factories = pluginManager.getExtensions(DDLCodecFactory.class);
		for(DDLCodecFactory factory:factories){
			LOGGER.info("init DDLCodecFactory pluginName: "+factory.getPluginName());
			Map<String,String> reqSettings = null;
			Map<String,String> respSettings = null;
			if(settings!=null&&settings.get(factory.getPluginName())!=null){
				reqSettings = settings.get(factory.getPluginName()).get("request");
				respSettings = settings.get(factory.getPluginName()).get("response");
			}
			if(factory.init(reqSettings,respSettings)){
				ddlFactories.put(factory.getPluginName().toLowerCase(), factory);
				cnt++;
				LOGGER.info("loaded DDLCodecFactory pluginName: "+factory.getPluginName());
			}
			else{
				LOGGER.info("failed to init DDLCodecFactory pluginName: "+factory.getPluginName());
			}
		}
		
		return cnt;
	}

	public static void main_bak(String args[]){
//		PluginMgr.getInstance().init(null);
		
		Map<String, Map<String,Map<String,String>>> map = new HashMap<String, Map<String,Map<String,String>>>();
		Map<String,Map<String,String>> value = new HashMap<String,Map<String,String>>();
		Map<String,String> respSetting = new HashMap<String,String>();

		respSetting.put("protocol","binary");
		respSetting.put("transport","raw");
		value.put("response",respSetting);
		map.put("thrift/0.10.0",value);
		Gson gson = new Gson();
		System.out.println(gson.toJson(map));
	}

	public DDLCodecFactory getDDLCodecFactoryByProtocol(String protocol) {
		for(DDLCodecFactory factory:ddlFactories.values()){
			if(protocol.equalsIgnoreCase(factory.getProtocolName())){
				return factory;
			}
		}
		return null;
	}
}
