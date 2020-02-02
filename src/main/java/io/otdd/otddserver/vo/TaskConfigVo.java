package io.otdd.otddserver.vo;

import lombok.Data;

@Data
public class TaskConfigVo {
	private String mockOutboundCalls;
	private String passthoughConnections;
}
