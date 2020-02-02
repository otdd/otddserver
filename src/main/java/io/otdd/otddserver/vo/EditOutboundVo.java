package io.otdd.otddserver.vo;

import io.otdd.otddserver.edit.EditOutboundCall;

public class EditOutboundVo {
	private int index;
	private int fromIndex;
	private int matchType;
	private String protocol;
	private byte[] request;
	private byte[] response;
	private Long responseTimestamp = 0L;
	
	public EditOutboundVo(){
		
	}
	public EditOutboundVo(EditOutboundCall call){
		this.index = call.getIndex();
		this.fromIndex = call.getFromIndex();
		this.matchType = call.getMatchType().ordinal();
		this.protocol = call.getProtocol();
		this.request = call.getReqBytes();
		this.response = call.getRespBytes();
		if(call.getRespTime()!=null){
			this.responseTimestamp = call.getRespTime().getTime();
		}
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getFromIndex() {
		return fromIndex;
	}
	public void setFromIndex(int fromIndex) {
		this.fromIndex = fromIndex;
	}
	public int getMatchType() {
		return matchType;
	}
	public void setMatchType(int matchType) {
		this.matchType = matchType;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
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
