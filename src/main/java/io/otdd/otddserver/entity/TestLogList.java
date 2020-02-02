package io.otdd.otddserver.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TestLogList {
	
	private long pulledTimestamp = 0;
	private List<Log> logs = new ArrayList<Log>();

}
