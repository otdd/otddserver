package io.otdd.otddserver.vo;

import io.otdd.otddserver.ddl.ParseFromDDLResult;
import lombok.Data;

@Data
public class ParseFromDDLResultVo {

	byte[] bytes;
	
	public ParseFromDDLResultVo(){
		
	}
	
	public ParseFromDDLResultVo(ParseFromDDLResult result){
		this.bytes = result.getBytes();
	}

}
