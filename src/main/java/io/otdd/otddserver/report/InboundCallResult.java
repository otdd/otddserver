package io.otdd.otddserver.report;

import lombok.Data;

import java.util.Date;

@Data
public class InboundCallResult {

	private byte[] reqBytes;
	private Date reqTime;
	private String reqErr;

	private byte[] respBytes;
	private Date respTime;
	private String respErr;

	private DiffResult reqDiff = new DiffResult();
	private DiffResult respDiff = new DiffResult();
}
