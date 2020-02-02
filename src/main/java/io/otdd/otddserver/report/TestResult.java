package io.otdd.otddserver.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class TestResult {

	private String testId;
    private int runId;
    private String testStoreType;
	private Date finishTime;

    private InboundCallResult inboundCallResult = new InboundCallResult();
    private List<OutboundCallResult> outboundCallResults = Collections.synchronizedList(new ArrayList<OutboundCallResult>());

}
