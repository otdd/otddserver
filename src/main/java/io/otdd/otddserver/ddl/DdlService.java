package io.otdd.otddserver.ddl;

import io.otdd.ddl.plugin.DDLCodecFactory;
import io.otdd.otddserver.plugin.PluginMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@EnableAutoConfiguration
public class DdlService {

	public enum ParseType{
		REQUEST,
		RESPONSE
	}

	public static final Logger LOGGER = LoggerFactory.getLogger(DdlService.class);

	public ParseToDDLResult parseToDDL(ParseType type,String pluginName,
			Map<String,String> pluginSettings,byte[] bytes){
		ParseToDDLResult ret = new ParseToDDLResult();
		ret.setErrCode(ParseToDDLResult.Code.FAILED);
		if(!StringUtils.isEmpty(pluginName)){
			ret.setPluginName(pluginName);
			DDLCodecFactory factory = PluginMgr.getInstance().getDDLCodecFactory(pluginName);
			if(factory==null||factory.getDecoder()==null){
				LOGGER.warn("no DDL decoder found for plguin:"+pluginName);
				ret.setErrCode(ParseToDDLResult.Code.NO_CODEC);
				return ret;
			}
			try{
				String ddl = null;
				if(type== ParseType.REQUEST){
					ddl = factory.getDecoder().decodeRequest(bytes,pluginSettings);
				}
				else if(type == ParseType.RESPONSE){
					ddl = factory.getDecoder().decodeResponse(bytes,pluginSettings);
				}
				if(!StringUtils.isEmpty(ddl)){
					ret.setProtocol(factory.getProtocolName());
					ret.setDdl(ddl);
					ret.setErrCode(ParseToDDLResult.Code.SUCCEED);
					return ret;
				}
			}
			catch(Exception e){
				e.printStackTrace();
				ret.setErrCode(ParseToDDLResult.Code.FAILED);
				ret.setErrMsg(e.getMessage());
				return ret;
			}
		}
		else{
			//try every decoder.
			Map<String,DDLCodecFactory> factories = PluginMgr.getInstance().getDDLCodecFactories();
			for(String key:factories.keySet()){
				DDLCodecFactory factory = factories.get(key);
				String ddl = null;
				try{
					if(type== ParseType.REQUEST){
						ddl = factory.getDecoder().decodeRequest(bytes,pluginSettings);
					}
					else if(type== ParseType.RESPONSE){
						ddl = factory.getDecoder().decodeResponse(bytes,pluginSettings);
					}
				}
				catch(Exception e){

				}
				if(ddl==null){// null means the packet is not valid for the protocol.
					continue;
				}
				else if(ddl.length()>0){
					ret.setPluginName(factory.getPluginName());
					ret.setDdl(ddl);
					ret.setErrCode(ParseToDDLResult.Code.SUCCEED);
					return ret;
				}
				else{
					ret.setErrCode(ParseToDDLResult.Code.FAILED);
					ret.setErrMsg("failed to parse");
					return ret;
				}
			}
			LOGGER.warn("no DDL decoder found for bytes:"+new String(bytes));
			ret.setErrCode(ParseToDDLResult.Code.NO_CODEC);
			return ret;
		}
		ret.setErrCode(ParseToDDLResult.Code.FAILED);
		return ret;
	}

	//type: 1.request; 2. response;
	public ParseFromDDLResult parseFromDDL(ParseType type,String pluginName,
			Map<String,String> pluginSettings,String ddl){
		ParseFromDDLResult ret = new ParseFromDDLResult();
		ret.setErrCode(ParseFromDDLResult.Code.FAILED);
		DDLCodecFactory factory = PluginMgr.getInstance().getDDLCodecFactory(pluginName);
		if(factory==null||factory.getEncoder()==null){
			LOGGER.warn("no DDL encoder found for pluginName:"+pluginName);
			ret.setErrCode(ParseFromDDLResult.Code.NO_CODEC);
			return ret;
		}
		try{
			byte[] bytes = null;
			if(type== ParseType.REQUEST){
				bytes = factory.getEncoder().encodeRequest(ddl,pluginSettings);
			}
			else if(type == ParseType.RESPONSE){
				bytes = factory.getEncoder().encodeResponse(ddl,pluginSettings);
			}
			if(bytes!=null){
				ret.setErrCode(ParseFromDDLResult.Code.SUCCEED);
				ret.setBytes(bytes);
				return ret;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			ret.setErrCode(ParseFromDDLResult.Code.FAILED);
			ret.setErrMsg(e.getMessage());
			return ret;
		}
		return ret;
	}

}
