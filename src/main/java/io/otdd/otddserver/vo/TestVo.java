package io.otdd.otddserver.vo;

import io.otdd.otddserver.testcase.InboundCallBase;
import io.otdd.otddserver.testcase.TestBase;
import io.otdd.otddserver.testcase.OutboundCallBase;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TestVo {
	private String id;
	private InboundVo inbound = new InboundVo();
	private List<OutboundVo> outbounds = new ArrayList<OutboundVo>();
	
	public TestVo(){
		
	}
	public TestVo(TestBase test){
		this.id = test.getId();
		this.inbound = new InboundVo((InboundCallBase)test.getInboundCall());
		for(OutboundCallBase call:test.getOutboundCalls()){
			OutboundVo vo = new OutboundVo(call);
			this.outbounds.add(vo);
		}
	}

}
