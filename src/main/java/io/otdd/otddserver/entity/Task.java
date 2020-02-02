package io.otdd.otddserver.entity;

import io.otdd.otddserver.vo.TaskTests;
import lombok.Data;

import java.util.Date;

@Data
public class Task {
    private Integer id;

    private Integer moduleId;

    private Integer targetPort;

    private Date createTime;

    private String status;

    private String target;

    private String config;

    private TaskTests tests;

}