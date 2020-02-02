package io.otdd.otddserver.vo;

import java.util.ArrayList;
import java.util.List;

public class TaskRunReportListVo {
	private PageInfoVo pageInfo = new PageInfoVo(20);
	List<TestLabelVo> testLabels = new ArrayList<TestLabelVo>();
	public PageInfoVo getPageInfo() {
		return pageInfo;
	}
	public void setPageInfo(PageInfoVo pageInfo) {
		this.pageInfo = pageInfo;
	}
	public List<TestLabelVo> getTestLabels() {
		return testLabels;
	}
	public void setTestLabels(List<TestLabelVo> testLabels) {
		this.testLabels = testLabels;
	}
}
