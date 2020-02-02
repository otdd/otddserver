package io.otdd.otddserver.vo;

import io.otdd.otddserver.entity.ReportedTest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ReportedTestListVo {
	private int runId;
	private boolean completed = false;
	private List<ReportedTest> reportedTests = new ArrayList<ReportedTest>();
	private PageInfoVo pageInfo = new PageInfoVo(10);
}
