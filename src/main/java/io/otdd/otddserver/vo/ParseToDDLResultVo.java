package io.otdd.otddserver.vo;

import io.otdd.otddserver.ddl.ParseToDDLResult;
import lombok.Data;

@Data
public class ParseToDDLResultVo {
	private String ddl;
	private String protocol;
	private String pluginName;
	
	public ParseToDDLResultVo(){
		
	}
	public ParseToDDLResultVo(ParseToDDLResult result){
		this.ddl = result.getDdl();
		this.pluginName = result.getPluginName();
		this.protocol = result.getProtocol();
	}
}
