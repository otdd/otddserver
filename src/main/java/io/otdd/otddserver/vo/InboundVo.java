package io.otdd.otddserver.vo;

import io.otdd.otddserver.testcase.InboundCallBase;

public class InboundVo {
	private byte[] request;
	private byte[] response;
	private Long responseTimestamp = 0L;
	
	public InboundVo(){
		
	}
	
	public InboundVo(InboundCallBase call){
		this.request = call.getReqBytes();
		this.response = call.getRespBytes();
		try{
			this.responseTimestamp = call.getRespTime().getTime();
		}
		catch(Exception e){
			
		}
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
