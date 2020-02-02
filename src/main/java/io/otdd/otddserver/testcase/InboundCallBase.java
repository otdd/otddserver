package io.otdd.otddserver.testcase;

import lombok.Data;

import java.util.Date;

@Data
public class InboundCallBase {
	protected byte[] reqBytes;
	protected String reqText;
	protected Date reqTime;
	
	protected byte[] respBytes;
	protected String respText;
	protected Date respTime;
	
}
