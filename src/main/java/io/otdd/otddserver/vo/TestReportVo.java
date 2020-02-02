package io.otdd.otddserver.vo;

import lombok.Data;

@Data
public class TestReportVo {
	private int runId;
	private String testId;
	private TestVo test;
	private TestResultVo result;

	public TestReportVo(int runId,String testId,TestVo test,TestResultVo result){
		this.runId = runId;
		this.testId = testId;
		this.test = test;
		this.result = result;
	}
}
