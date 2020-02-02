package io.otdd.otddserver.entity;

import lombok.Data;

@Data
public class RunProgress {

    private Integer runId;

    private Integer complted;

    private Integer total;

    private Integer percent;

}