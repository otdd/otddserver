package io.otdd.otddserver.vo;

import io.otdd.otddserver.testcase.OutboundCallBase;
import io.otdd.otddserver.edit.EditInboundCall;
import io.otdd.otddserver.edit.EditTest;
import io.otdd.otddserver.edit.EditOutboundCall;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EditTestVo {
	private String id;
	private String fromId;
	private int moduleId;
	private EditInboundVo inbound = new EditInboundVo();
	private List<EditOutboundVo> outbounds = new ArrayList<EditOutboundVo>();
	public EditTestVo(){
		
	}
	public EditTestVo(EditTest test){
		this.id = test.getId();
		this.fromId = test.getFromId();
		this.moduleId = test.getModuleId();
		this.inbound = new EditInboundVo((EditInboundCall)test.getInboundCall());
		for(OutboundCallBase tmp:test.getOutboundCalls()){
			EditOutboundCall call = (EditOutboundCall)tmp;
			this.outbounds.add(new EditOutboundVo(call));
		}
	}
	
}
