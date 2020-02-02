package io.otdd.otddserver.service;

import io.otdd.otddserver.entity.PageBean;
import io.otdd.otddserver.entity.ReportedTest;
import io.otdd.otddserver.entity.TaskRun;
import io.otdd.otddserver.report.InboundCallResult;
import io.otdd.otddserver.report.OutboundCallResult;
import io.otdd.otddserver.report.TestResult;
import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.testcase.OutboundCallType;
import io.otdd.otddserver.util.PaddingUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@EnableAutoConfiguration
public class ReportService extends BaseService{
    private String indexPath = "testreports";

    @Autowired
    TaskRunService taskRunService;

    public PageBean<ReportedTest> getTaskRunReportList(int runId, int current, int pageSize) {
        PageBean<ReportedTest> ret = new PageBean<>();
        ret.setCurPage(current);
        ret.setPageSize(pageSize);

        DirectoryReader reader = openDirectoryReader(indexPath);
        if(reader==null){
            ret.setTotalNum(0);
            return ret;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            int MAX_RESULTS = 10000;
            Term term = new Term("runId",PaddingUtil.paddingLong(runId));
            Query query = new TermQuery(term);
            SortField sf = new SortField("finishTime", SortField.Type.STRING, true);
            Sort sort = new Sort(sf);
            int start = pageSize*(current-1);
            TopFieldCollector collector = TopFieldCollector.create(sort,start+pageSize,MAX_RESULTS);
            searcher.search(query,collector);
            TopDocs docs = collector.topDocs(start,pageSize);
            ret.setTotalNum((int)docs.totalHits.value);
            if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
                for(int i=0;i<docs.scoreDocs.length;i++) {
                    Document doc = searcher.doc(docs.scoreDocs[i].doc);
                    ReportedTest tmp = new ReportedTest();
                    tmp.setRunId(runId);
                    tmp.setStartTime(new Date(Long.parseLong(doc.getField("inbound.reqTime").getCharSequenceValue().toString())));
                    tmp.setEndTime(new Date(Long.parseLong(doc.getField("finishTime").getCharSequenceValue().toString())));
                    tmp.setTestId(doc.getField("testId").getCharSequenceValue().toString());
                    ret.getData().add(tmp);
                }
            }
            return ret;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{reader.close();}catch (Exception e){e.printStackTrace();}
        }
        return ret;
    }

    public void saveTestResult(TestResult result) {
        System.out.println("result:"+result);

        IndexWriter indexWriter = openIndexWriter(indexPath);
        if(indexWriter==null){
            return;
        }
        try {
            TaskRun taskRun = taskRunService.getById(result.getRunId());
            Document doc = new Document();
            doc.add(new Field("runId", PaddingUtil.paddingLong(result.getRunId()), StringField.TYPE_STORED));
            doc.add(new Field("moduleId", PaddingUtil.paddingLong(taskRun.getModuleId()), StringField.TYPE_STORED));
            doc.add(new Field("testStoreType", result.getTestStoreType(), StringField.TYPE_STORED));
            doc.add(new Field("testId", result.getTestId(), StringField.TYPE_STORED));
            doc.add(new Field("finishTime", PaddingUtil.paddingLong(result.getFinishTime().getTime()), TextField.TYPE_STORED));
            doc.add(new SortedDocValuesField("finishTime", new BytesRef(PaddingUtil.paddingLong(result.getFinishTime().getTime()))));

            if(result.getInboundCallResult()!=null) {
                if(result.getInboundCallResult().getReqBytes()!=null&&result.getInboundCallResult().getReqBytes().length>0) {
                    doc.add(new Field("inbound.reqBytes", result.getInboundCallResult().getReqBytes(), StoredField.TYPE));
                }
                if(result.getInboundCallResult().getReqTime()!=null){
                    doc.add(new Field("inbound.reqTime", PaddingUtil.paddingLong(result.getInboundCallResult().getReqTime().getTime()), TextField.TYPE_STORED));
                }
                if(StringUtils.isNotBlank(result.getInboundCallResult().getReqErr())){
                    doc.add(new Field("inbound.reqErr", result.getInboundCallResult().getReqErr(), TextField.TYPE_STORED));
                }
                if(result.getInboundCallResult().getRespBytes()!=null&&result.getInboundCallResult().getRespBytes().length>0) {
                    doc.add(new Field("inbound.respBytes", result.getInboundCallResult().getRespBytes(), StoredField.TYPE));
                }
                if(result.getInboundCallResult().getRespTime()!=null){
                    doc.add(new Field("inbound.respTime", PaddingUtil.paddingLong(result.getInboundCallResult().getRespTime().getTime()), TextField.TYPE_STORED));
                }
                if(StringUtils.isNotBlank(result.getInboundCallResult().getRespErr())){
                    doc.add(new Field("inbound.respErr", result.getInboundCallResult().getRespErr(), TextField.TYPE_STORED));
                }
            }
            if(result.getOutboundCallResults()!=null&&result.getOutboundCallResults().size()>0) {
                for (int i = 0; i < result.getOutboundCallResults().size(); i++) {
                    OutboundCallResult outboundCall = result.getOutboundCallResults().get(i);
                    doc.add(new Field("outbound." + i + ".matchedIndex", PaddingUtil.paddingLong(outboundCall.getMatchedPeerIndex()), StoredField.TYPE));
                    doc.add(new Field("outbound." + i + ".type", PaddingUtil.paddingLong(outboundCall.getType().ordinal()), StoredField.TYPE));
                    doc.add(new Field("outbound." + i + ".isPassthrough", outboundCall.isPassthrough()?"1":"0", StoredField.TYPE));
                    if (outboundCall.getReqBytes() != null) {
                        doc.add(new Field("outbound." + i + ".reqBytes", outboundCall.getReqBytes(), StoredField.TYPE));
                    }
                    if (outboundCall.getReqTime() != null) {
                        doc.add(new Field("outbound." + i + ".reqTime", PaddingUtil.paddingLong(outboundCall.getReqTime().getTime()), TextField.TYPE_STORED));
                    }
                    if (outboundCall.getRespBytes() != null) {
                        doc.add(new Field("outbound." + i + ".respBytes", outboundCall.getRespBytes(), StoredField.TYPE));
                    }
                    if (StringUtils.isNotBlank(outboundCall.getRespErr())) {
                        doc.add(new Field("outbound." + i + ".respErr", outboundCall.getRespErr(), StoredField.TYPE));
                    }
                    if (outboundCall.getRespTime() != null) {
                        doc.add(new Field("outbound." + i + ".respTime", PaddingUtil.paddingLong(outboundCall.getRespTime().getTime()), TextField.TYPE_STORED));
                    }
                }
            }

            indexWriter.addDocument(doc);
            indexWriter.flush();

            return;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{indexWriter.close();} catch (Exception e){e.printStackTrace();}
        }
        return;

    }

    public TestResult getTestResult(int runId, String testId) {
        DirectoryReader reader = openDirectoryReader(indexPath);
        if(reader==null){
            return null;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            int MAX_RESULTS = 10000;
            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            PhraseQuery q = new PhraseQuery("runId",PaddingUtil.paddingLong(runId));
            booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            q = new PhraseQuery("testId",testId);
            booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            TopDocs docs = searcher.search(booleanQueryBuilder.build(),1);
            if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
                Document doc = searcher.doc(docs.scoreDocs[0].doc);
                return convertDocToTestResult(doc);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{reader.close();}catch (Exception e){e.printStackTrace();}
        }
        return null;
    }

    private TestResult convertDocToTestResult(Document doc) {
        if(doc==null){
            return null;
        }
        TestResult result = new TestResult();
        InboundCallResult inboundCallBase = null;
        Map<Integer, OutboundCallResult> outboundCalls = new TreeMap<>();
        Pattern outboundReqPattern = Pattern.compile("outbound\\.([0-9]+)\\.reqBytes");
        Pattern outboundReqTimePattern = Pattern.compile("outbound\\.([0-9]+)\\.reqTime");
        Pattern outboundRespPattern = Pattern.compile("outbound\\.([0-9]+)\\.respBytes");
        Pattern outboundRespErrPattern = Pattern.compile("outbound\\.([0-9]+)\\.respErr");
        Pattern outboundRespTimePattern = Pattern.compile("outbound\\.([0-9]+)\\.respTime");

        Pattern outboundMatchedIndexPattern = Pattern.compile("outbound\\.([0-9]+)\\.matchedIndex");
        Pattern outboundTypePattern = Pattern.compile("outbound\\.([0-9]+)\\.type");
        Pattern outboundIsPassthroughPattern = Pattern.compile("outbound\\.([0-9]+)\\.isPassthrough");

        for(IndexableField field:doc.getFields()){
            Matcher outboundReqMatcher = outboundReqPattern.matcher(field.name());
            Matcher outboundReqTimeMatcher = outboundReqTimePattern.matcher(field.name());
            Matcher outboundRespMatcher = outboundRespPattern.matcher(field.name());
            Matcher outboundRespErrMatcher = outboundRespErrPattern.matcher(field.name());
            Matcher outboundRespTimeMatcher = outboundRespTimePattern.matcher(field.name());

            Matcher outboundMatchedIndexMatcher = outboundMatchedIndexPattern.matcher(field.name());
            Matcher outboundTypeMatcher = outboundTypePattern.matcher(field.name());
            Matcher outboundIsPassthroughMatcher = outboundIsPassthroughPattern.matcher(field.name());

            if("runId".equals(field.name())){
                result.setRunId(Integer.parseInt(field.getCharSequenceValue().toString()));
            }
            else if("testId".equals(field.name())){
                result.setTestId(field.getCharSequenceValue().toString());
            }
            else if("testStoreType".equals(field.name())){
                String testStoreType = field.getCharSequenceValue().toString();
                if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(testStoreType)){
                    result.setTestStoreType(TestStoreType.ONLINE_RECORDED_TEST);
                }
                else{
                    result.setTestStoreType(TestStoreType.EDITED_TEST);
                }
            }
            else if("finishTime".equals(field.name())){
                result.setFinishTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
            else if("inbound.reqBytes".equals(field.name())){
                if(inboundCallBase==null){
                    inboundCallBase = new InboundCallResult();
                }
                inboundCallBase.setReqBytes(field.binaryValue().bytes);
            }
            else if("inbound.reqErr".equals(field.name())){
                if(inboundCallBase==null){
                    inboundCallBase = new InboundCallResult();
                }
                inboundCallBase.setReqErr(field.getCharSequenceValue().toString());
            }
            else if("inbound.reqTime".equals(field.name())){
                if(inboundCallBase==null){
                    inboundCallBase = new InboundCallResult();
                }
                inboundCallBase.setReqTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
            else if("inbound.respBytes".equals(field.name())){
                if(inboundCallBase==null){
                    inboundCallBase = new InboundCallResult();
                }
                inboundCallBase.setRespBytes(field.binaryValue().bytes);
            }
            else if("inbound.respErr".equals(field.name())){
                if(inboundCallBase==null){
                    inboundCallBase = new InboundCallResult();
                }
                inboundCallBase.setRespErr(field.getCharSequenceValue().toString());
            }
            else if("inbound.respTime".equals(field.name())){
                if(inboundCallBase==null){
                    inboundCallBase = new InboundCallResult();
                }
                inboundCallBase.setRespTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
            else if(outboundMatchedIndexMatcher.matches()){
                Integer index = Integer.parseInt(outboundMatchedIndexMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    outboundCalls.put(index,new OutboundCallResult());
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setMatchedPeerIndex(Integer.parseInt(field.getCharSequenceValue().toString()));
            }
            else if(outboundTypeMatcher.matches()){
                Integer index = Integer.parseInt(outboundTypeMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    outboundCalls.put(index,new OutboundCallResult());
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setType(OutboundCallType.values()[Integer.parseInt(field.getCharSequenceValue().toString())]);
            }
            else if(outboundIsPassthroughMatcher.matches()){
                Integer index = Integer.parseInt(outboundIsPassthroughMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    outboundCalls.put(index,new OutboundCallResult());
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setPassthrough(Integer.parseInt(field.getCharSequenceValue().toString())==1?true:false);
            }
            else if(outboundReqMatcher.matches()){
                Integer index = Integer.parseInt(outboundReqMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    outboundCalls.put(index,new OutboundCallResult());
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setReqBytes(field.binaryValue().bytes);
            }
            else if(outboundReqTimeMatcher.matches()){
                Integer index = Integer.parseInt(outboundReqTimeMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    outboundCalls.put(index,new OutboundCallResult());
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setReqTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
            else if(outboundRespMatcher.matches()){
                Integer index = Integer.parseInt(outboundRespMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    outboundCalls.put(index,new OutboundCallResult());
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setRespBytes(field.binaryValue().bytes);
            }
            else if(outboundRespErrMatcher.matches()){
                Integer index = Integer.parseInt(outboundRespErrMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    outboundCalls.put(index,new OutboundCallResult());
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setRespErr(field.getCharSequenceValue().toString());
            }
            else if(outboundRespTimeMatcher.matches()){
                Integer index = Integer.parseInt(outboundRespTimeMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    outboundCalls.put(index,new OutboundCallResult());
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setRespTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
        }
        result.setInboundCallResult(inboundCallBase);
        outboundCalls.values().forEach(
                outboundCall->{
                    result.getOutboundCallResults().add(outboundCall);
                }
        );
        return result;
    }
}
