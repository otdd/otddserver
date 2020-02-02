package io.otdd.otddserver.testcase;

import lombok.Data;

@Data
public class Test extends TestBase{
	public Test(){
		this.inboundCall = new InboundCall();
	}
	
}
