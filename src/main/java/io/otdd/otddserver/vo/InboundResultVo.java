package io.otdd.otddserver.vo;

import io.otdd.otddserver.report.InboundCallResult;
import lombok.Data;

@Data
public class InboundResultVo {

	private byte[] request;
	private String requestError;
	private byte[] response;
	private String responseError;

	private Long responseTimestamp = 0L;
	
	private DiffResultVo reqDiff = new DiffResultVo();
	private DiffResultVo respDiff = new DiffResultVo();
	
	public InboundResultVo(){
		
	}
	public InboundResultVo(InboundCallResult result){
		this.request = result.getReqBytes();
		this.requestError = result.getReqErr();
		this.response = result.getRespBytes();
		this.responseError = result.getRespErr();
		try{
			this.responseTimestamp = result.getRespTime().getTime();
		}
		catch(Exception e){
			
		}
		this.reqDiff = new DiffResultVo(result.getReqDiff());
		this.respDiff = new DiffResultVo(result.getRespDiff());
	}
}
