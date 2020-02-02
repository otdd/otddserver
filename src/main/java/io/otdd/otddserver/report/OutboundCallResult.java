package io.otdd.otddserver.report;

import io.otdd.otddserver.testcase.OutboundCallType;
import lombok.Data;

import java.util.Date;

@Data
public class OutboundCallResult {
	private int index;
	private int matchedPeerIndex = -1;

	private byte[] reqBytes;
	private Date reqTime;
	
	private byte[] respBytes;
	private Date respTime;
	private String respErr;

	private OutboundCallType type = OutboundCallType.REQUEST_AND_RESPONSE;
	
	private boolean isPassthrough = false;
	
	private DiffResult reqDiff = new DiffResult();
	private DiffResult respDiff = new DiffResult();
	
}
