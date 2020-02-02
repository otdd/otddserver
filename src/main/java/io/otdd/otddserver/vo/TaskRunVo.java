package io.otdd.otddserver.vo;

import io.otdd.otddserver.entity.TaskRun;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TaskRunVo {
	private Integer id;

    private Integer taskId;

    private String startTime;

    private String endTime;

    private String status;
    
    public TaskRunVo(){
    	
    }
    
    public TaskRunVo(TaskRun run){
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	if(run.getEndTime()!=null){
    		this.endTime = df.format(run.getEndTime());
    	}
		this.id = run.getId();
		if(run.getStartTime()!=null){
			this.startTime = df.format(run.getStartTime());
		}
		this.status = run.getStatus();
		this.taskId = run.getTaskId();
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
