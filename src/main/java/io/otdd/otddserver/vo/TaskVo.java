package io.otdd.otddserver.vo;

import com.google.gson.Gson;
import io.otdd.otddserver.entity.Task;
import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Data
public class TaskVo {
	private int id;
	private int moduleId;
	private String createTime;
	private String status;
	private TaskConfigVo config = new TaskConfigVo();
	private String target;
	private int targetPort;
	private TaskTests tests = new TaskTests();
	
	public TaskVo(){
		
	}
	
	public TaskVo(Task task){
		Gson gson = new Gson();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.id = task.getId();
		this.moduleId = task.getModuleId();
		this.createTime = df.format(task.getCreateTime());
		this.status = task.getStatus();
		this.config = gson.fromJson(task.getConfig(),TaskConfigVo.class);
		this.target = task.getTarget();
		this.tests = task.getTests();
		this.targetPort = task.getTargetPort();
	}

}


