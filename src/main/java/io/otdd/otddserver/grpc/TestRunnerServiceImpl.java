package io.otdd.otddserver.grpc;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.otdd.otddserver.FetchOutboundRespResp;
import io.otdd.otddserver.ReportTestResultResp;
import io.otdd.otddserver.TestCase;
import io.otdd.otddserver.TestRunnerServiceGrpc;
import io.otdd.otddserver.entity.FetchTestCase;
import io.otdd.otddserver.match.MatchResult;
import io.otdd.otddserver.report.OutboundCallResult;
import io.otdd.otddserver.report.TestResult;
import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.service.LogService;
import io.otdd.otddserver.service.MatchService;
import io.otdd.otddserver.service.ReportService;
import io.otdd.otddserver.service.RunTestService;
import io.otdd.otddserver.testcase.OutboundCallBase;
import io.otdd.otddserver.testcase.OutboundCallType;
import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.testcase.TestBase;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class TestRunnerServiceImpl extends TestRunnerServiceGrpc.TestRunnerServiceImplBase {

    @Autowired
    private RunTestService runTestService;

    @Autowired
    MatchService matchService;

    @Autowired
    ReportService reportService;

    @Autowired
    LogService logService;

    //cache 30 minutes. enough for debugging while running the test.
    private ExpiringMap testCache = ExpiringMap.builder().expiration(30, TimeUnit.MINUTES).build();
    private ExpiringMap resultCache = ExpiringMap.builder().expiration(30, TimeUnit.MINUTES).build();

    @Override
    public void fetchTestCase(io.otdd.otddserver.FetchTestCaseReq request,
                              io.grpc.stub.StreamObserver<io.otdd.otddserver.TestCase> responseObserver) {

        FetchTestCase testCase = runTestService.fetchTestCase(request.getUsername(),request.getTag(),request.getMac());
        if(testCase!=null&&testCase.getTest()!=null){
            String key = testCase.getRunId()+"_"+testCase.getTest().getId();
            testCache.put(key,testCase.getTest());
            TestCase resp = TestCase.newBuilder().setTestId(testCase.getTest().getId())
                    .setInboundRequest(ByteString.copyFrom(testCase.getTest().getInboundCall().getReqBytes()))
                    .setPort(testCase.getPort())
                    .setMockOutboundConnections(testCase.getMockOutboundConnections())
                    .addAllPassthroughConnections(testCase.getPassthroughConnections())
                    .setRunId(""+testCase.getRunId())
                    .build();
            responseObserver.onNext(resp);

            TestResult testResult = new TestResult();
            testResult.setRunId(testCase.getRunId());
            testResult.setTestId(testCase.getTest().getId());
            if(testCase.getTest() instanceof Test) {
                testResult.setTestStoreType(TestStoreType.ONLINE_RECORDED_TEST);
            }
            else{
                testResult.setTestStoreType(TestStoreType.EDITED_TEST);
            }
            testResult.getInboundCallResult().setReqBytes(testCase.getTest().getInboundCall().getReqBytes());
            testResult.getInboundCallResult().setReqTime(new Date());
            resultCache.put(key,testResult);
        }
        else{
            responseObserver.onNext(null);
        }
        responseObserver.onCompleted();

    }

    @Override
    public void fetchOutboundResp(io.otdd.otddserver.FetchOutboundRespReq request,
                                  io.grpc.stub.StreamObserver<io.otdd.otddserver.FetchOutboundRespResp> responseObserver) {
        String key = request.getRunId()+"_"+request.getTestId();
        TestBase test = (TestBase)testCache.get(key);
        TestResult result = (TestResult)resultCache.get(key);
        if(test!=null){

            OutboundCallResult outboundCallResult = new OutboundCallResult();
            outboundCallResult.setReqBytes(request.getOutboundReq().toByteArray());
            outboundCallResult.setReqTime(new Date());
            result.getOutboundCallResults().add(outboundCallResult);

            // it's for greeting msg.
            // e.g. the mysql client connects and send nothing first but waits for the mysql server's greeting msg.
            if(request.getOutboundReq()==null||request.getOutboundReq().size()==0){
                int index = 0;
                for(OutboundCallBase outboundCall:test.getOutboundCalls()){
                    if(outboundCall.getReqBytes()==null||outboundCall.getReqBytes().length==0){

                        FetchOutboundRespResp resp = FetchOutboundRespResp.newBuilder().setOutboundResp(ByteString.copyFrom(outboundCall.getRespBytes()))
                                .build();
                        responseObserver.onNext(resp);
                        responseObserver.onCompleted();

                        outboundCallResult.setMatchedPeerIndex(index);
                        outboundCallResult.setRespBytes(outboundCall.getRespBytes());
                        outboundCallResult.setRespTime(new Date());
                        outboundCallResult.setType(OutboundCallType.CONNECT_AND_RECEIVE_GREETING);
                        return;
                    }
                    index++;
                }
            }
            MatchResult matchResult = matchService.getMostMatchedIndex(test,request.getOutboundReq().toByteArray());
            if(matchResult!=null&&matchResult.getMatchedIndex()>=0){
                OutboundCallBase matchedCall = test.getOutboundCalls().get(matchResult.getMatchedIndex());
                FetchOutboundRespResp resp = FetchOutboundRespResp.newBuilder().setOutboundResp(ByteString.copyFrom(matchedCall.getRespBytes()))
                        .build();
                responseObserver.onNext(resp);
                responseObserver.onCompleted();

                outboundCallResult.setMatchedPeerIndex(matchResult.getMatchedIndex());
                outboundCallResult.setRespBytes(matchedCall.getRespBytes());
                outboundCallResult.setRespTime(new Date());
                outboundCallResult.setType(OutboundCallType.REQUEST_AND_RESPONSE);
                return;
            }
            else{
                outboundCallResult.setMatchedPeerIndex(-1);
                outboundCallResult.setRespErr("no response is matched! so this connection is forcibly closed by otdd test runner.");
                outboundCallResult.setType(OutboundCallType.REQUEST_AND_RESPONSE);
            }
        }
        responseObserver.onNext(null);
        responseObserver.onCompleted();
    }

    @Override
    public void reportTestResult(io.otdd.otddserver.TestResult request,
                                 io.grpc.stub.StreamObserver<io.otdd.otddserver.ReportTestResultResp> responseObserver) {
        System.out.println("TestResult:"+request);

        String key = request.getRunId()+"_"+request.getTestId();
        TestResult result = (TestResult)resultCache.get(key);
        result.setFinishTime(new Date());
        if(result!=null) {
            result.getInboundCallResult().setReqErr(request.getInboundRequestErr());
            result.getInboundCallResult().setRespBytes(request.getInboundResponse().toByteArray());
            result.getInboundCallResult().setRespErr(request.getInboundResponseErr());
            result.getInboundCallResult().setRespTime(new Date());
            reportService.saveTestResult(result);
            ReportTestResultResp resp = ReportTestResultResp.newBuilder().setResult(true)
                    .build();
            responseObserver.onNext(resp);
        }
        else{
            responseObserver.onNext(null);
        }
        responseObserver.onCompleted();
        testCache.remove(key);
        resultCache.remove(key);
    }

    public void log(io.otdd.otddserver.LogReq request,
                    io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
        System.out.println("Log:"+request);
        logService.log(request.getTestId(),Integer.parseInt(request.getRunId()),request.getLog(),request.getTimestamp(),request.getLevel());
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

}

