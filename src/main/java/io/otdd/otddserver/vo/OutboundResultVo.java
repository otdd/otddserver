package io.otdd.otddserver.vo;

import io.otdd.otddserver.report.OutboundCallResult;
import lombok.Data;

@Data
public class OutboundResultVo {
	private int index;
	private int matchedPeerIndex;
	private byte[] request;
	private byte[] response;
	private String responseError;
	private Long responseTimestamp = 0L;
	private boolean isPassthrough = false;
	private boolean isHandshake = false;
	private DiffResultVo reqDiff = new DiffResultVo();
	private DiffResultVo respDiff = new DiffResultVo();
	
	public OutboundResultVo(){
		
	}
	public OutboundResultVo(OutboundCallResult result){
		if(result==null){
			return;
		}
		this.index = result.getIndex();
		this.matchedPeerIndex = result.getMatchedPeerIndex();
		this.request = result.getReqBytes();
		this.response = result.getRespBytes();
		this.responseError = result.getRespErr();
		try{
			this.responseTimestamp = result.getRespTime().getTime();
		}
		catch(Exception e){
			
		}
		this.isPassthrough = result.isPassthrough();
		this.reqDiff = new DiffResultVo(result.getReqDiff());
		this.respDiff = new DiffResultVo(result.getRespDiff());
	}

}