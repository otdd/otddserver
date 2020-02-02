package io.otdd.otddserver.vo;

import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.search.SearchResult;
import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.edit.EditTest;

import java.util.ArrayList;
import java.util.List;

public class SearchResultVo{
	private PageInfoVo pageInfo = new PageInfoVo(40);
	private List<TestLabelVo> testLabels = new ArrayList<TestLabelVo>();

	public SearchResultVo(){

	}
	public SearchResultVo(SearchResult result){
		this.pageInfo.setCurrent(result.getCurPage());
		this.pageInfo.setPageSize(result.getPageSize());
		this.pageInfo.setTotal(result.getTotalNum());
		for(Object test:result.getTests()){
			if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(result.getTestStoreType()) ){
				TestLabelVo testLabelVo = new TestLabelVo((Test)test);
				this.testLabels.add(testLabelVo);
			}
			else if(TestStoreType.EDITED_TEST.equalsIgnoreCase(result.getTestStoreType())){
				TestLabelVo testLabelVo = new TestLabelVo((EditTest)test);
				this.testLabels.add(testLabelVo);
			}
		}
	}
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
