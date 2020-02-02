package io.otdd.otddserver.testcase;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class TestBase {
	
	protected String id;

	protected String moduleName;

	private String inboundProtocol;

	protected int moduleId;

	protected Date insertTime;

	protected InboundCallBase inboundCall;
	
	protected List<OutboundCallBase> outboundCalls = new ArrayList<OutboundCallBase>();

}
