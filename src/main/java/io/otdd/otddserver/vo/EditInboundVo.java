package io.otdd.otddserver.vo;

import io.otdd.otddserver.edit.EditInboundCall;

public class EditInboundVo {
	private byte[] request;
	private byte[] response;
	private Long responseTimestamp = 0L;
	
	public EditInboundVo(){
		
	}
	public EditInboundVo(EditInboundCall call){
		this.request = call.getReqBytes();
		this.response = call.getRespBytes();
		this.responseTimestamp = call.getRespTime().getTime();
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
