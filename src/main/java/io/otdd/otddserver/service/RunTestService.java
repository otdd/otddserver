package io.otdd.otddserver.service;

import com.google.gson.Gson;
import io.otdd.otddserver.edit.EditTest;
import io.otdd.otddserver.entity.FetchTestCase;
import io.otdd.otddserver.entity.RunProgress;
import io.otdd.otddserver.entity.Task;
import io.otdd.otddserver.entity.TaskRun;
import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.testcase.TestBase;
import io.otdd.otddserver.util.PaddingUtil;
import io.otdd.otddserver.vo.TaskConfigVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@EnableAutoConfiguration
public class RunTestService extends BaseService{
    @Autowired
    private TargetService targetService;

    @Autowired
    private TestService testService;

    @Autowired
    private EditService editService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRunService taskRunService;

    private String indexPath = "runtests";

    private String testsCntIndexPath = "runtestscnt";

    public boolean createRunTests(int runId, String from,List<String> testIds){
        IndexWriter indexWriter = openIndexWriter(indexPath);
        if(indexWriter==null){
            return false;
        }
        try {
            for(String testId:testIds) {
                Document doc = new Document();
                doc.add(new Field("runId", PaddingUtil.paddingLong(runId), StringField.TYPE_STORED));
                doc.add(new Field("testStoreType", from, StringField.TYPE_STORED));
                doc.add(new Field("testId", testId, StringField.TYPE_STORED));
                doc.add(new Field("fetched", "0", StringField.TYPE_STORED));
                indexWriter.addDocument(doc);
            }
            saveTestsCnt(runId,testIds.size());
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{indexWriter.close();} catch (Exception e){e.printStackTrace();}
        }
        return false;
    }

    public boolean saveTestsCnt(int runId, int size){
        IndexWriter indexWriter = openIndexWriter(testsCntIndexPath);
        if(indexWriter==null){
            return false;
        }
        try {
            Document doc = new Document();
            doc.add(new Field("runId", PaddingUtil.paddingLong(runId), StringField.TYPE_STORED));
            doc.add(new Field("testCnt", ""+size, StringField.TYPE_STORED));
            indexWriter.addDocument(doc);
            indexWriter.flush();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{indexWriter.close();} catch (Exception e){e.printStackTrace();}
        }
        return false;
    }

    public int getTestsCnt(int runId){
        DirectoryReader reader = openDirectoryReader(testsCntIndexPath);
        if(reader==null){
            return 0;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            PhraseQuery q = new PhraseQuery("runId", PaddingUtil.paddingLong(runId));
            TopDocs docs = searcher.search(q,1);
            if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
                Document doc = searcher.doc(docs.scoreDocs[0].doc);
                return Integer.parseInt(doc.getField("testCnt").getCharSequenceValue().toString());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{reader.close();}catch (Exception e){e.printStackTrace();}
        }
        return 0;
    }

    public FetchTestCase fetchTestCase(String username, String tag, String mac) {
        targetService.createOrUpdateTaskTarget(username,tag,mac);
        String fullTarget = username +"."+tag+"."+mac;
        TaskRun taskRun = taskRunService.findActiveTaskRun(fullTarget);
        if(taskRun==null){
            return null;
        }
        Task task = taskService.getTask(taskRun.getTaskId());
        if(task==null){
            return null;
        }
        FetchTestCase ret = new FetchTestCase();
        ret.setRunId(taskRun.getId());
        ret.setPort(task.getTargetPort());
        ret.setMockOutboundConnections(1);
        Gson gson = new Gson();
        if(StringUtils.isNotBlank(task.getConfig())){
            TaskConfigVo config = gson.fromJson(task.getConfig(), TaskConfigVo.class);
            if(StringUtils.isNotBlank(config.getPassthoughConnections())){
                for(String tmp:config.getPassthoughConnections().split(",")){
                    ret.getPassthroughConnections().add(tmp);
                }
            }
            ret.setMockOutboundConnections("no".equalsIgnoreCase(config.getMockOutboundCalls())?0:1);
        }
        TestBase test = findUnFetchedTestCase(taskRun.getId(),task.getModuleId());
        if(test==null){
            taskRun.setStatus(TaskRun.ENDED);
            taskRun.setEndTime(new Date());
            taskRunService.createOrUpdateTaskRun(taskRun);
            return null;
        }
        else{
            if(taskRun.getStartTime()==null){
                taskRun.setStartTime(new Date());
                taskRunService.createOrUpdateTaskRun(taskRun);
            }
        }
        ret.setTest(test);
        return ret;
    }

    private TestBase findUnFetchedTestCase(Integer runId, Integer moduleId) {
        DirectoryReader reader = openDirectoryReader(indexPath);
        if(reader==null){
            return null;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            PhraseQuery q = new PhraseQuery("runId", PaddingUtil.paddingLong(runId));
            TopDocs docs = searcher.search(q,1);
            if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
                Document doc = searcher.doc(docs.scoreDocs[0].doc);
                String testId = doc.getField("testId").getCharSequenceValue().toString();
                String testStoreType = doc.getField("testStoreType").getCharSequenceValue().toString();
                setFetched(runId,testId,testStoreType);
                if(TestStoreType.EDITED_TEST.equalsIgnoreCase(testStoreType)) {
                    EditTest test = editService.getEditTestById(testId);
                    return test;
                }
                else{
                    Test test = testService.getTestById(moduleId, testId);
                    return test;
                }
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

    private boolean setFetched(Integer runId, String testId,String from) {
        IndexWriter indexWriter = openIndexWriter(indexPath);
        if(indexWriter==null){
            return false;
        }
        try {
            //delete
            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            PhraseQuery q = new PhraseQuery("runId", PaddingUtil.paddingLong(runId));
            booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            q = new PhraseQuery("testId", testId);
            booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            indexWriter.deleteDocuments(booleanQueryBuilder.build());
            indexWriter.flush();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{indexWriter.close();} catch (Exception e){e.printStackTrace();}
        }
        return false;
    }

    public RunProgress getRunProgress(int runId) {

        RunProgress ret = new RunProgress();
        ret.setRunId(runId);
        DirectoryReader reader = openDirectoryReader(indexPath);
        if(reader==null){
            return null;
        }
        try {
            ret.setTotal(getTestsCnt(runId));
            if(ret.getTotal()<=0){
                return null;
            }
            IndexSearcher searcher = new IndexSearcher(reader);
            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            PhraseQuery q = new PhraseQuery("runId", PaddingUtil.paddingLong(runId));
            booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            q = new PhraseQuery("fetched", "0");
            booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            TopDocs docs = searcher.search(booleanQueryBuilder.build(),1);
            ret.setComplted(ret.getTotal()-(int)docs.totalHits.value);
            ret.setPercent((ret.getComplted()*100)/ret.getTotal());
            return ret;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{reader.close();}catch (Exception e){e.printStackTrace();}
        }

        return null;
    }
}
