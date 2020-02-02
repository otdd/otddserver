package io.otdd.otddserver.vo;

public class ModuleListQueryVo {
	
	private PageInfoVo pageInfo = new PageInfoVo(20);
	
	public PageInfoVo getPageInfo() {
		return pageInfo;
	}
	public void setPageInfo(PageInfoVo pageInfo) {
		this.pageInfo = pageInfo;
	}
}
