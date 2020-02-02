package io.otdd.otddserver.entity;

import lombok.Data;

import java.util.Date;

@Data
public class TaskTarget {
    private String username;
    private String tag;
    private String mac;
    private Date lastActiveTime;
}
