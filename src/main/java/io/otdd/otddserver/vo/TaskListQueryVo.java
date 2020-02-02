package io.otdd.otddserver.vo;

public class TaskListQueryVo {
	
	private int moduleId;
	
	private PageInfoVo pageInfo = new PageInfoVo(20);
	
	public int getModuleId() {
		return moduleId;
	}
	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}
	public PageInfoVo getPageInfo() {
		return pageInfo;
	}
	public void setPageInfo(PageInfoVo pageInfo) {
		this.pageInfo = pageInfo;
	}
}
