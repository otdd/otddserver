package io.otdd.otddserver.service;

import io.otdd.otddserver.entity.*;
import io.otdd.otddserver.util.PaddingUtil;
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

@Service
@EnableAutoConfiguration
public class TaskRunService extends BaseService{

	@Autowired
	private TargetService targetService;

	@Autowired
	private TaskService taskService;

	private String taskRunIndexPath = "taskruns";

	public int newTaskRun(TaskRun taskRun) {
		taskRun.setId(null);
		if(createOrUpdateTaskRun(taskRun)!=null){
			return taskRun.getId();
		}
		return -1;
	}

	private int findMaxTaskRunId() {
		DirectoryReader reader = openDirectoryReader(taskRunIndexPath);
		if(reader==null){
			return 0;
		}
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			MatchAllDocsQuery q = new MatchAllDocsQuery();
			SortField sf = new SortField("runId", SortField.Type.STRING, true);
			Sort sort = new Sort(sf);
			TopFieldDocs docs = searcher.search(q,1,sort);
			if(docs!=null&&docs.scoreDocs!=null&&docs.scoreDocs.length>0){
				Document doc = searcher.doc(docs.scoreDocs[0].doc);
				return Integer.parseInt(doc.getField("runId").getCharSequenceValue().toString());
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

	private TaskRun convertDocToTaskRun(Document doc) {
		if(doc==null){
			return null;
		}
		TaskRun taskRun = new TaskRun();
		for(IndexableField field:doc.getFields()){
			if("runId".equals(field.name())){
				taskRun.setId(Integer.parseInt(field.getCharSequenceValue().toString()));
			}
			if("taskId".equals(field.name())){
				taskRun.setTaskId(Integer.parseInt(field.getCharSequenceValue().toString()));
			}
			if("moduleId".equals(field.name())){
				taskRun.setModuleId(Integer.parseInt(field.getCharSequenceValue().toString()));
			}
			else if("status".equals(field.name())){
				taskRun.setStatus(field.getCharSequenceValue().toString());
			}
			else if("target".equals(field.name())){
				taskRun.setTarget(field.getCharSequenceValue().toString());
			}
			else if("startTime".equals(field.name())){
				taskRun.setStartTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
			}
			else if("endTime".equals(field.name())){
				taskRun.setEndTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
			}
			else if("createTime".equals(field.name())){
				taskRun.setCreateTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
			}
		}
		return taskRun;
	}

	public PageBean<TaskRun> getTaskRunList(TaskRunListQuery query) {
		PageBean<TaskRun> ret = new PageBean<TaskRun>();
		ret.setPageSize(query.getPageSize());
		ret.setCurPage(query.getCurPage());
		DirectoryReader reader = openDirectoryReader(taskRunIndexPath);
		if(reader==null){
			ret.setTotalNum(0);
			return ret;
		}
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			Term term = new Term("taskId",PaddingUtil.paddingLong(query.getTaskId()));
			Query q = new TermQuery(term);
			int MAX_RESULTS = 10000;
//			TopScoreDocCollector collector = TopScoreDocCollector.create(query.getPageSize(),MAX_RESULTS);  // MAX_RESULTS is just an int limiting the total number of hits
//			MatchAllDocsQuery q = new MatchAllDocsQuery();
			SortField sf = new SortField("runId", SortField.Type.STRING, true);
			Sort sort = new Sort(sf);
			int start = query.getPageSize()*(query.getCurPage()-1);
			TopFieldCollector collector = TopFieldCollector.create(sort,start+query.getPageSize(),MAX_RESULTS);
			searcher.search(q,collector);
			TopDocs docs = collector.topDocs(start,query.getPageSize());
			ret.setTotalNum((int)docs.totalHits.value);
			if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
				for(int i=0;i<docs.scoreDocs.length;i++) {
					Document doc = searcher.doc(docs.scoreDocs[i].doc);
					ret.getData().add(convertDocToTaskRun(doc));
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

	public TaskRun createOrUpdateTaskRun(TaskRun taskRun) {

		IndexWriter indexWriter = openIndexWriter(taskRunIndexPath);
		if(indexWriter==null){
			return null;
		}
		try {

			if(taskRun.getId()==null) {
				int tmp = findMaxTaskRunId();
				taskRun.setId(tmp+1);
			}

			//delete
			Term term = new Term("runId",PaddingUtil.paddingLong(taskRun.getId()));
			indexWriter.deleteDocuments(term);
			indexWriter.flush();

			//add
			Document doc = new Document();
			if(taskRun.getCreateTime()==null){
				taskRun.setCreateTime(new Date());
			}
			doc.add(new Field("runId", PaddingUtil.paddingLong(taskRun.getId()), StringField.TYPE_STORED));
			//https://stackoverflow.com/questions/29695307/sortiing-string-field-alphabetically-in-lucene-5-0
			doc.add(new SortedDocValuesField("runId", new BytesRef(PaddingUtil.paddingLong(taskRun.getId()))));
			doc.add(new Field("taskId", PaddingUtil.paddingLong(taskRun.getTaskId()), StringField.TYPE_STORED));
			doc.add(new Field("moduleId", PaddingUtil.paddingLong(taskRun.getModuleId()), StringField.TYPE_STORED));
			doc.add(new Field("status",taskRun.getStatus(), StringField.TYPE_STORED));
			doc.add(new Field("target",taskRun.getTarget(), StringField.TYPE_STORED));
			doc.add(new Field("createTime", PaddingUtil.paddingLong(taskRun.getCreateTime().getTime()), TextField.TYPE_STORED));
			if(taskRun.getStartTime()!=null) {
				doc.add(new Field("startTime", PaddingUtil.paddingLong(taskRun.getStartTime().getTime()), TextField.TYPE_STORED));
			}
			if(taskRun.getEndTime()!=null) {
				doc.add(new Field("endTime", PaddingUtil.paddingLong(taskRun.getEndTime().getTime()), TextField.TYPE_STORED));
			}
			indexWriter.addDocument(doc);
			indexWriter.flush();
			return taskRun;
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			try{indexWriter.close();} catch (Exception e){e.printStackTrace();}
		}
		return null;
	}

	public TaskRun findActiveTaskRun(String fullTarget) {
		DirectoryReader reader = openDirectoryReader(taskRunIndexPath);
		if(reader==null){
			return null;
		}
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
			PhraseQuery q = new PhraseQuery("target",fullTarget);
			booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
			q = new PhraseQuery("status",TaskRun.CREATED);
			booleanQueryBuilder.add(q,BooleanClause.Occur.SHOULD);
			q = new PhraseQuery("status",TaskRun.RUNNING);
			booleanQueryBuilder.add(q,BooleanClause.Occur.SHOULD);
			booleanQueryBuilder.setMinimumNumberShouldMatch(1);

			SortField sf = new SortField("runId", SortField.Type.STRING, false);
			Sort sort = new Sort(sf);
			TopDocs docs = searcher.search(booleanQueryBuilder.build(),1,sort);
			if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
				Document doc = searcher.doc(docs.scoreDocs[0].doc);
				return convertDocToTaskRun(doc);
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

	public TaskRun getById(int runId) {
		DirectoryReader reader = openDirectoryReader(taskRunIndexPath);
		if(reader==null){
			return null;
		}
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			PhraseQuery q = new PhraseQuery("runId",PaddingUtil.paddingLong(runId));
			TopDocs docs = searcher.search(q,1);
			if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
				Document doc = searcher.doc(docs.scoreDocs[0].doc);
				return convertDocToTaskRun(doc);
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
}
