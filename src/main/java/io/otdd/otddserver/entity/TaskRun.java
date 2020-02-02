package io.otdd.otddserver.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TaskRun {

    public static String CREATED = "created";
    public static String RUNNING = "running";
    public static String ENDED = "ended";

    private Integer id;

    private Integer taskId;

    private Integer moduleId;

    private Date createTime;

    private Date startTime;

    private Date endTime;

    private String status;

    private String target;

}