package io.otdd.otddserver.service;

import com.google.gson.Gson;
import io.otdd.otddserver.entity.*;
import io.otdd.otddserver.search.SearchQuery;
import io.otdd.otddserver.search.SearchResult;
import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.testcase.TestBase;
import io.otdd.otddserver.util.PaddingUtil;
import io.otdd.otddserver.vo.SearchQueryVo;
import io.otdd.otddserver.vo.TaskTests;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@EnableAutoConfiguration
public class TaskService extends BaseService{

    @Autowired
    private  TaskRunService taskRunService;

    @Autowired
    private  RunTestService runTestService;

    @Autowired
    private  TestService testService;

    @Autowired
    private  EditService editService;

    private String taskIndexPath = "tasks";

    public int newTask(int moduleId, String target, int targetPort,String config, TaskTests tests) {

        IndexWriter indexWriter = openIndexWriter(taskIndexPath);
        if(indexWriter==null){
            return -1;
        }
        try {
            int tmp = findMaxTaskId();
            int id = tmp+1;
            Document doc = new Document();
            doc.add(new Field("taskId", PaddingUtil.paddingLong(id), StringField.TYPE_STORED));
            doc.add(new Field("moduleId", PaddingUtil.paddingLong(moduleId), StringField.TYPE_STORED));
            doc.add(new Field("targetPort", PaddingUtil.paddingLong(targetPort), StringField.TYPE_STORED));
            //https://stackoverflow.com/questions/29695307/sortiing-string-field-alphabetically-in-lucene-5-0
            doc.add(new SortedDocValuesField("taskId", new BytesRef(PaddingUtil.paddingLong(id))));
            doc.add(new Field("config", ""+config, StringField.TYPE_STORED));
            doc.add(new Field("target", ""+target, StringField.TYPE_STORED));
            Gson gson = new Gson();
            doc.add(new Field("tests", gson.toJson(tests), StringField.TYPE_STORED));
            doc.add(new Field("createTime", PaddingUtil.paddingLong(new Date().getTime()), TextField.TYPE_STORED));
            indexWriter.addDocument(doc);
            indexWriter.flush();

            TaskRun taskRun = new TaskRun();
            taskRun.setModuleId(moduleId);
            taskRun.setTaskId(id);
            taskRun.setCreateTime(new Date());
            taskRun.setStatus(TaskRun.CREATED);
            taskRun.setTarget(target);
            int runId = taskRunService.newTaskRun(taskRun);

            if(tests.getType()==TaskTests.TYPE_FROM_IDS) {
                runTestService.createRunTests(runId, tests.getTestStoreType(),tests.getTestIds());
            }
            else{
                SearchQuery query = changeSearchQuery(tests.getQuery());
                query.setCurPage(1);
                query.setPageSize(100);


                SearchResult result = null;
                if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(tests.getTestStoreType())) {
                    result = testService.search(query);
                }
                else {
                    result = editService.search(query);
                }
                List<String> testIds = new ArrayList<>();
                while (result.getTests() != null && result.getTests().size() > 0) {
                    for (Object t : result.getTests()) {
                        testIds.add(((TestBase) t).getId());
                    }
                    query.setCurPage(result.getCurPage() + 1);
                    query.setPageSize(result.getPageSize());
                    if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(tests.getTestStoreType())) {
                        result = testService.search(query);
                    }
                    else {
                        result = editService.search(query);
                    }
                }
                runTestService.createRunTests(runId, tests.getTestStoreType(), testIds);
            }
            return id;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{indexWriter.close();} catch (Exception e){e.printStackTrace();}
        }
        return -1;
    }

    public boolean rerun(int taskId) {
        Task task = getTask(taskId);
        if(task==null){
            return false;
        }
        TaskRun taskRun = new TaskRun();
        taskRun.setTaskId(taskId);
        taskRun.setModuleId(task.getModuleId());
        taskRun.setCreateTime(new Date());
        taskRun.setStatus(TaskRun.CREATED);
        taskRun.setTarget(task.getTarget());
        int runId = taskRunService.newTaskRun(taskRun);

        if(task.getTests().getType()==TaskTests.TYPE_FROM_IDS) {
            runTestService.createRunTests(runId, task.getTests().getTestStoreType(),task.getTests().getTestIds());
            return true;
        }
        else{
            //
        }

        return false;
    }

    public Task getTask(int id) {
        DirectoryReader reader = openDirectoryReader(taskIndexPath);
        if(reader==null){
            return null;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            Term term = new Term("taskId",PaddingUtil.paddingLong(id));
            Query query = new TermQuery(term);
            ScoreDoc[] hits = searcher.search(query, 1).scoreDocs;
            if(hits!=null&&hits.length>0){
                Document doc = searcher.doc(hits[0].doc);
                return convertDocToTask(doc);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{
                reader.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public Task getLastTask(int moduleId) {
        DirectoryReader reader = openDirectoryReader(taskIndexPath);
        if(reader==null){
            return null;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            Term term = new Term("moduleId",PaddingUtil.paddingLong(moduleId));
            Query query = new TermQuery(term);
            SortField sf = new SortField("taskId", SortField.Type.STRING, true);
            Sort sort = new Sort(sf);
            ScoreDoc[] hits = searcher.search(query, 1,sort).scoreDocs;
            if(hits!=null&&hits.length>0){
                Document doc = searcher.doc(hits[0].doc);
                return convertDocToTask(doc);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{
                reader.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public PageBean<Task> getTaskList(int moduleId,int curPage,int pageSize) {
        PageBean<Task> ret = new PageBean<>();
        ret.setPageSize(pageSize);
        ret.setCurPage(curPage);
        DirectoryReader reader = openDirectoryReader(taskIndexPath);
        if(reader==null){
            ret.setTotalNum(0);
            return ret;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            int MAX_RESULTS = 10000;
//			TopScoreDocCollector collector = TopScoreDocCollector.create(query.getPageSize(),MAX_RESULTS);  // MAX_RESULTS is just an int limiting the total number of hits
            Term term = new Term("moduleId",PaddingUtil.paddingLong(moduleId));
            Query query = new TermQuery(term);
            SortField sf = new SortField("taskId", SortField.Type.STRING, true);
            Sort sort = new Sort(sf);
            int start = pageSize*(curPage-1);
            TopFieldCollector collector = TopFieldCollector.create(sort,start+pageSize,MAX_RESULTS);
            searcher.search(query,collector);
            TopDocs docs = collector.topDocs(pageSize*(curPage-1),pageSize);
            ret.setTotalNum((int)docs.totalHits.value);
            if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
                for(int i=0;i<docs.scoreDocs.length;i++) {
                    Document doc = searcher.doc(docs.scoreDocs[i].doc);
                    ret.getData().add(convertDocToTask(doc));
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

    private Task convertDocToTask(Document doc) {
        if(doc==null){
            return null;
        }
        Task task = new Task();
        for(IndexableField field:doc.getFields()){
            if("taskId".equals(field.name())){
                task.setId(Integer.parseInt(field.getCharSequenceValue().toString()));
            }
            if("moduleId".equals(field.name())){
                task.setModuleId(Integer.parseInt(field.getCharSequenceValue().toString()));
            }
            if("targetPort".equals(field.name())){
                task.setTargetPort(Integer.parseInt(field.getCharSequenceValue().toString()));
            }
            else if("tests".equals(field.name())){
                Gson gson = new Gson();
                task.setTests(gson.fromJson(field.getCharSequenceValue().toString(),TaskTests.class));
            }
            else if("target".equals(field.name())){
                task.setTarget(field.getCharSequenceValue().toString());
            }
            else if("config".equals(field.name())){
                task.setConfig(field.getCharSequenceValue().toString());
            }
            else if("createTime".equals(field.name())){
                task.setCreateTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
        }
        return task;
    }

    private int findMaxTaskId() {
        DirectoryReader reader = openDirectoryReader(taskIndexPath);
        if(reader==null){
            return 0;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            MatchAllDocsQuery q = new MatchAllDocsQuery();
            SortField sf = new SortField("taskId", SortField.Type.STRING, true);
            Sort sort = new Sort(sf);
            TopFieldDocs docs = searcher.search(q,1,sort);
            if(docs!=null&&docs.scoreDocs!=null&&docs.scoreDocs.length>0){
                Document doc = searcher.doc(docs.scoreDocs[0].doc);
                return Integer.parseInt(doc.getField("taskId").getCharSequenceValue().toString());
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

    private SearchQuery changeSearchQuery(SearchQueryVo vo) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SearchQuery ret = new SearchQuery();
        ret.setInboundReq(vo.getInboundReq());
        ret.setInboundResp(vo.getInboundResp());
        ret.setCurPage(vo.getPageInfo().getCurrent());
        if(!StringUtils.isEmpty(vo.getEndTime())){
            try{
                ret.setEndTime(df.parse(vo.getEndTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ret.setPageSize(vo.getPageInfo().getPageSize());
        if(!StringUtils.isEmpty(vo.getStartTime())){
            try{
                ret.setStartTime(df.parse(vo.getStartTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ret.setModuleId(vo.getModuleId());
        ret.setTestId(vo.getTestId());
        ret.setOutboundReq(vo.getOutboundReq());
        ret.setOutboundResp(vo.getOutboundResp());
        if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(vo.getTestStoreType())){
            ret.setTestStoreType(TestStoreType.ONLINE_RECORDED_TEST);
        }
        else if(TestStoreType.EDITED_TEST.equalsIgnoreCase(vo.getTestStoreType())){
            ret.setTestStoreType(TestStoreType.EDITED_TEST);
        }
        return ret;
    }
}
