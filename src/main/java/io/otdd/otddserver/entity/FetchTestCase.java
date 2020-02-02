package io.otdd.otddserver.entity;

import io.otdd.otddserver.testcase.TestBase;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FetchTestCase {
    int port;
    int runId;
    int mockOutboundConnections;
    List<String> passthroughConnections = new ArrayList<>();
    TestBase test;
}
