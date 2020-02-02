package io.otdd.otddserver.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Log {

    private String testId;

    private int runId;

    private String log;

    private long logTime;

    private String level;

}