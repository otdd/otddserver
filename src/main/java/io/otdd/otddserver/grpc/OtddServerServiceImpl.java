package io.otdd.otddserver.grpc;

import io.otdd.otddserver.OtddServerServiceGrpc;
import io.otdd.otddserver.SaveTestCaseResp;
import io.otdd.otddserver.entity.Module;
import io.otdd.otddserver.service.TestService;
import io.otdd.otddserver.service.ModuleService;
import io.otdd.otddserver.testcase.InboundCall;
import io.otdd.otddserver.testcase.OutboundCall;
import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.util.TestIdGenerator;
import io.otdd.otddserver.util.TextUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.util.Date;
import java.util.UUID;

@Component
public class OtddServerServiceImpl extends OtddServerServiceGrpc.OtddServerServiceImplBase {

    @Autowired
    private TestService testService;

    @Autowired
    private ModuleService moduleService;

    @Override
    public void saveTestCase(io.otdd.otddserver.SaveTestCaseReq request,
                             io.grpc.stub.StreamObserver<io.otdd.otddserver.SaveTestCaseResp> responseObserver){
        System.out.println("SaveTestCaseReq:"+request);

        SaveTestCaseResp grpcResp = null;

        try {
            Test testCase = new Test();
//            JSONObject json = JSONObject.fromObject(request.getTestCase());
            JSONObject json = JSONObject.fromObject(
                    "{\"module\":\"reviews-v2\",\"protocol\":\"http\",\"inbound\":{\"req\":\"R0VUIC9yZXZpZXdzLzAgSFRUUC8xLjENCg0K\",\"req_time\":\"1579362187407\",\"resp\":\"SFRUUC8xLjEgMjAwIE9LDQpYLVBvd2VyZWQtQnk6IFNlcnZsZXQvMy4xDQpDb250ZW50LVR5cGU6IGFwcGxpY2F0aW9uL2pzb24NCkRhdGU6IFNhdCwgMTggSmFuIDIwMjAgMDY6MTg6MTcgR01UDQpDb250ZW50LUxhbmd1YWdlOiBlbi1VUw0KQ29udGVudC1MZW5ndGg6IDM3OQ0KDQp7ImlkIjogIjAiLCJyZXZpZXdzIjogW3sgICJyZXZpZXdlciI6ICJSZXZpZXdlcjEiLCAgInRleHQiOiAiQW4gZXh0cmVtZWx5IGVudGVydGFpbmluZyBwbGF5IGJ5IFNoYWtlc3BlYXJlLiBUaGUgc2xhcHN0aWNrIGh1bW91ciBpcyByZWZyZXNoaW5nISIsICJyYXRpbmciOiB7InN0YXJzIjogMiwgImNvbG9yIjogImJsYWNrIn19LHsgICJyZXZpZXdlciI6ICJSZXZpZXdlcjIiLCAgInRleHQiOiAiQWJzb2x1dGVseSBmdW4gYW5kIGVudGVydGFpbmluZy4gVGhlIHBsYXkgbGFja3MgdGhlbWF0aWMgZGVwdGggd2hlbiBjb21wYXJlZCB0byBvdGhlciBwbGF5cyBieSBTaGFrZXNwZWFyZS4iLCAicmF0aW5nIjogeyJzdGFycyI6IDQsICJjb2xvciI6ICJibGFjayJ9fV19\",\"resp_time\":\"1579362187405\",},\"outbound\":[{\"req\":\"R0VUIC9yYXRpbmdzLzAgSFRUUC8xLjENCkFjY2VwdDogYXBwbGljYXRpb24vanNvbg0KVXNlci1BZ2VudDogQXBhY2hlLUNYRi8zLjEuMTgNCkNhY2hlLUNvbnRyb2w6IG5vLWNhY2hlDQpQcmFnbWE6IG5vLWNhY2hlDQpIb3N0OiAxMjcuMC4wLjE6OTA4MA0KQ29ubmVjdGlvbjoga2VlcC1hbGl2ZQ0KDQo\",\"req_time\":\"1579362187405\",\"resp\":\"SFRUUC8xLjEgMjAwIE9LDQpjb250ZW50LWxlbmd0aDogNDgNCg0KeyJpZCI6MCwicmF0aW5ncyI6eyJSZXZpZXdlcjEiOjIsIlJldmlld2VyMiI6NH19\",\"resp_time\":\"1579362187405\"}]}"
            );
            testCase.setId(TestIdGenerator.generateId());
            testCase.setModuleName(json.getString("module"));
            testCase.setInboundProtocol(json.getString("protocol"));
            Module m = moduleService.getOrCreateModuleByName(testCase.getModuleName(),testCase.getInboundProtocol());
            testCase.setModuleId(m.getId());
            JSONObject inbound = json.optJSONObject("inbound");
            if(inbound!=null){
                InboundCall inboundCall = new InboundCall();
                String req = inbound.optString("req");
                if(!StringUtils.isBlank(req)){
                    inboundCall.setReqBytes(Base64Utils.decodeFromString(req));
                    inboundCall.setReqText(TextUtil.removeNonPrintable(new String(inboundCall.getReqBytes())));
                    String reqTime = inbound.optString("req_time");
                    if(!StringUtils.isBlank(reqTime)) {
                        inboundCall.setReqTime(new Date(Long.parseLong(reqTime)));
                    }
                }
                String resp = inbound.optString("resp");
                if(!StringUtils.isBlank(resp)){
                    inboundCall.setRespBytes(Base64Utils.decodeFromString(resp));
                    inboundCall.setRespText(TextUtil.removeNonPrintable(new String(inboundCall.getRespBytes())));
                    String respTime = inbound.optString("resp_time");
                    if(!StringUtils.isBlank(respTime)) {
                        inboundCall.setRespTime(new Date(Long.parseLong(respTime)));
                    }
                }
                testCase.setInboundCall(inboundCall);
            }

            JSONArray outbounds = json.optJSONArray("outbound");
            if(outbounds!=null&&outbounds.size()>0){
                for(int i=0;i<outbounds.size();i++){
                    JSONObject outbound = outbounds.getJSONObject(i);
                    OutboundCall outboundCall = new OutboundCall();
                    outboundCall.setIndex(i);
                    String req = outbound.optString("req");
                    if(!StringUtils.isBlank(req)){
                        outboundCall.setReqBytes(Base64Utils.decodeFromString(req));
                        outboundCall.setReqText(TextUtil.removeNonPrintable(new String(outboundCall.getReqBytes())));
                        String reqTime = outbound.optString("req_time");
                        if(!StringUtils.isBlank(reqTime)) {
                            outboundCall.setReqTime(new Date(Long.parseLong(reqTime)));
                        }
                    }
                    String resp = outbound.optString("resp");
                    if(!StringUtils.isBlank(resp)){
                        outboundCall.setRespBytes(Base64Utils.decodeFromString(resp));
                        outboundCall.setRespText(TextUtil.removeNonPrintable(new String(outboundCall.getRespBytes())));
                        String respTime = outbound.optString("resp_time");
                        if(!StringUtils.isBlank(respTime)) {
                            outboundCall.setRespTime(new Date(Long.parseLong(respTime)));
                        }
                    }
                    testCase.getOutboundCalls().add(outboundCall);
                }
            }

            testService.saveTest(testCase);
            System.out.println("test saved, id:"+testCase.getId());

            grpcResp = SaveTestCaseResp.newBuilder().setResult(true).build();

        }
        catch (Exception e){
            e.printStackTrace();
            grpcResp = SaveTestCaseResp.newBuilder().setResult(false).build();
        }

        responseObserver.onNext(grpcResp);
        responseObserver.onCompleted();
    }
}
