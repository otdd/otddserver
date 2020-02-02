package io.otdd.otddserver.edit;

import io.otdd.otddserver.match.MatchType;
import io.otdd.otddserver.testcase.OutboundCall;
import io.otdd.otddserver.testcase.OutboundCallBase;
import lombok.Data;

@Data
public class EditOutboundCall extends OutboundCallBase {
	
	private int fromIndex = -1;
	private MatchType matchType = MatchType.SIMILARITY;
	private String protocol = null;
	
	public EditOutboundCall(){
		
	}
	public EditOutboundCall(OutboundCall call) {
		this.index = call.getIndex();
		this.fromIndex = call.getIndex();
		this.reqBytes = call.getReqBytes();
		this.reqText = call.getReqText();
		this.respBytes = call.getRespBytes();
		this.respText = call.getRespText();
		this.respTime = call.getRespTime();
//		this.type = call.getType();
	}
}
