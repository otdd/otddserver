package io.otdd.otddserver.edit;

import io.otdd.otddserver.testcase.InboundCall;
import io.otdd.otddserver.testcase.InboundCallBase;
import lombok.Data;

@Data
public class EditInboundCall extends InboundCallBase {
	
	public EditInboundCall(){
		
	}
	public EditInboundCall(InboundCall call) {
		this.reqBytes = call.getReqBytes();
		this.reqText = call.getReqText();
		this.reqTime = call.getReqTime();
		this.respBytes = call.getRespBytes();
		this.respText = call.getRespText();
		this.respTime = call.getRespTime();
	}
	
}
