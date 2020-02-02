package io.otdd.otddserver.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Module {
    
    private Integer id;

    private String name;

    private String protocol;

    private String pluginConf;

    private Date createTime;

}