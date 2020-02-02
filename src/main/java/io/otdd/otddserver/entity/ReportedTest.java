package io.otdd.otddserver.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ReportedTest {

    private Integer runId;

    private String testId;

    private Date startTime;

    private Date endTime;

}