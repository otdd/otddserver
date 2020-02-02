package io.otdd.otddserver.vo;

import io.otdd.otddserver.testcase.OutboundCallBase;

public class OutboundVo {
	private int index;
	private byte[] request;
	private byte[] response;
	private Long responseTimestamp = 0L;
	
	public OutboundVo(){
		
	}
	public OutboundVo(OutboundCallBase call){
		this.index = call.getIndex();
		this.request = call.getReqBytes();
		this.response = call.getRespBytes();
		try{
			this.responseTimestamp = call.getRespTime().getTime();
		}
		catch(Exception e){
		}
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public byte[] getRequest() {
		return request;
	}
	public void setRequest(byte[] request) {
		this.request = request;
	}
	public byte[] getResponse() {
		return response;
	}
	public void setResponse(byte[] response) {
		this.response = response;
	}
	public Long getResponseTimestamp() {
		return responseTimestamp;
	}
	public void setResponseTimestamp(Long responseTimestamp) {
		this.responseTimestamp = responseTimestamp;
	}
	
}
