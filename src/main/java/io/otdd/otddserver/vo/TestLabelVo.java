package io.otdd.otddserver.vo;

import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.edit.EditTest;

public class TestLabelVo {
	private String id;
	private String label;
	
	public TestLabelVo(){
		
	}
	public TestLabelVo(Test test){
		this.id = test.getId();
		String label = test.getInboundCall().getReqText();
		this.label = label.substring(0, label.length()>100?100:label.length());
	}
	public TestLabelVo(EditTest test){
		this.id = test.getId();
		String label = test.getInboundCall().getReqText();
		this.label = label.substring(0, label.length()>100?100:label.length());
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
