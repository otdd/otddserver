package io.otdd.otddserver.ddl;

import lombok.Data;

@Data
public class ParseToDDLResult {
	public enum Code{
		SUCCEED,
		FAILED,
		NO_CODEC
	}
	private String ddl;
	private String pluginName;
	private String protocol;
	private Code errCode = Code.SUCCEED;
	private String errMsg = "";
}
