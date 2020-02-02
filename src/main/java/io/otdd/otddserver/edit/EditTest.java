package io.otdd.otddserver.edit;

import io.otdd.otddserver.testcase.*;
import io.otdd.otddserver.util.TestIdGenerator;
import lombok.Data;

@Data
public class EditTest extends TestBase {
	
	private String fromId;

	public EditTest(){
		this.inboundCall = new EditInboundCall();
	}
	
	public EditTest(Test test) {
		this.id = TestIdGenerator.generateId();
		this.fromId = test.getId();
		this.moduleId = test.getModuleId();
		this.moduleName = test.getModuleName();
		this.inboundCall = new EditInboundCall((InboundCall)test.getInboundCall());
		for(OutboundCallBase tmp:test.getOutboundCalls()){
			OutboundCall call = (OutboundCall)tmp;
			this.outboundCalls.add(new EditOutboundCall(call));
		}
		
	}

}
