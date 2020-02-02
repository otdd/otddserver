package io.otdd.otddserver.vo;

import io.otdd.otddserver.entity.PageBean;
import io.otdd.otddserver.entity.Task;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskListVo {
	private PageInfoVo pageInfo = new PageInfoVo(20);
	private List<TaskVo> tasks = new ArrayList<TaskVo>();
	
	public TaskListVo(){
		
	}
	
	public TaskListVo(PageBean<Task> list){
		this.pageInfo.setCurrent(list.getCurPage());
		this.pageInfo.setPageSize(list.getPageSize());
		this.pageInfo.setTotal(list.getTotalNum());
		for(Task taskRun:list.getData()){
			this.tasks.add(new TaskVo(taskRun));
		}
	}
	
}
