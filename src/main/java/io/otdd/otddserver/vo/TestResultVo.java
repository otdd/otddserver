package io.otdd.otddserver.vo;

import io.otdd.otddserver.report.OutboundCallResult;
import io.otdd.otddserver.report.TestResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TestResultVo {
	
    private InboundResultVo inboundResult = new InboundResultVo();
    private List<OutboundResultVo> outboundResults = new ArrayList<OutboundResultVo>();
	
	public TestResultVo(){
		
	}
	public TestResultVo(TestResult result){
		if(result==null){
			return;
		}
		this.inboundResult = new InboundResultVo(result.getInboundCallResult());
		for(OutboundCallResult tmp:result.getOutboundCallResults()){
			OutboundResultVo vo = new OutboundResultVo(tmp);
			this.outboundResults.add(vo);
		}
	}
}
