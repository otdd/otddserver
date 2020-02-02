package io.otdd.otddserver.vo;

public class TaskRunListQueryVo {
	
	private int taskId;
	private PageInfoVo pageInfo = new PageInfoVo(20);
	
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	public PageInfoVo getPageInfo() {
		return pageInfo;
	}
	public void setPageInfo(PageInfoVo pageInfo) {
		this.pageInfo = pageInfo;
	}
}
