package io.otdd.otddserver.service;

import io.otdd.otddserver.entity.Module;
import io.otdd.otddserver.entity.PageBean;
import io.otdd.otddserver.util.PaddingUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@EnableAutoConfiguration
public class ModuleService extends BaseService {

	private String indexPath = "modules";

	public Module getModule(int moduleId) {
		DirectoryReader reader = openDirectoryReader(indexPath);
		if(reader==null){
			return null;
		}
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			Term term = new Term("moduleId",PaddingUtil.paddingLong(moduleId));
			Query query = new TermQuery(term);
			ScoreDoc[] hits = searcher.search(query, 1).scoreDocs;
			if(hits!=null&&hits.length>0){
				Document doc = searcher.doc(hits[0].doc);
				return convertDocToModule(doc);
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

	public Module getOrCreateModuleByName(String name,String protocol) {
		DirectoryReader reader = openDirectoryReader(indexPath);
		if(reader==null){
			return createModuleByName(name,protocol);
		}
		try {
			Analyzer analyzer = new StandardAnalyzer();
			IndexSearcher searcher = new IndexSearcher(reader);
			Term term = new Term("name",name);
			Query query = new TermQuery(term);
			ScoreDoc[] hits = searcher.search(query, 1).scoreDocs;
			if(hits!=null&&hits.length>0) {
				Document doc = searcher.doc(hits[0].doc);
				return convertDocToModule(doc);
			}
			else{
				return createModuleByName(name,protocol);
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

	private Module convertDocToModule(Document doc) {
		if(doc==null){
			return null;
		}
		Module module = new Module();
		for(IndexableField field:doc.getFields()){
			if("moduleId".equals(field.name())){
				module.setId(Integer.parseInt(field.getCharSequenceValue().toString()));
			}
			else if("name".equals(field.name())){
				module.setName(field.getCharSequenceValue().toString());
			}
			else if("protocol".equals(field.name())){
				module.setProtocol(field.getCharSequenceValue().toString());
			}
			else if("pluginConf".equals(field.name())){
				module.setPluginConf(field.getCharSequenceValue().toString());
			}
			else if("createTime".equals(field.name())){
				module.setCreateTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
			}
		}
		return module;
	}

	private Module createModuleByName(String name,String protocol) {

		Module module = new Module();
		module.setName(name);
		module.setProtocol(protocol);
		if(createModule(module)){
			return module;
		}
		return null;
	}

	private int findMaxModuleId() {
		DirectoryReader reader = openDirectoryReader(indexPath);
		if(reader==null){
			return 0;
		}
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			MatchAllDocsQuery q = new MatchAllDocsQuery();
			SortField sf = new SortField("moduleId", SortField.Type.STRING, true);
			Sort sort = new Sort(sf);
			TopFieldDocs docs = searcher.search(q,1,sort);
			if(docs!=null&&docs.scoreDocs!=null&&docs.scoreDocs.length>0){
				Document doc = searcher.doc(docs.scoreDocs[0].doc);
				return Integer.parseInt(doc.getField("moduleId").getCharSequenceValue().toString());
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

	public PageBean<Module> getModuleList(int curPage,int pageSize) {
		PageBean<Module> ret = new PageBean<Module>();
		ret.setPageSize(pageSize);
		ret.setCurPage(curPage);
		DirectoryReader reader = openDirectoryReader(indexPath);
		if(reader==null){
			ret.setTotalNum(0);
			return ret;
		}
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			int MAX_RESULTS = 10000;
//			TopScoreDocCollector collector = TopScoreDocCollector.create(query.getPageSize(),MAX_RESULTS);  // MAX_RESULTS is just an int limiting the total number of hits
			MatchAllDocsQuery q = new MatchAllDocsQuery();
			SortField sf = new SortField("moduleId", SortField.Type.STRING, false);
			Sort sort = new Sort(sf);
			int start = pageSize*(curPage-1);
			TopFieldCollector collector = TopFieldCollector.create(sort,start+pageSize,MAX_RESULTS);
			searcher.search(q,collector);
			TopDocs docs = collector.topDocs(start,pageSize);
			ret.setTotalNum((int)docs.totalHits.value);
			if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
				for(int i=0;i<docs.scoreDocs.length;i++) {
					Document doc = searcher.doc(docs.scoreDocs[i].doc);
					ret.getData().add(convertDocToModule(doc));
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

	public boolean createModule(Module module) {
		IndexWriter indexWriter = openIndexWriter(indexPath);
		if(indexWriter==null){
			return false;
		}
		try {
			int id = findMaxModuleId();
			module.setId(id+1);
			if(module.getCreateTime()==null){
				module.setCreateTime(new Date());
			}
			Document doc = new Document();
			doc.add(new Field("moduleId", PaddingUtil.paddingLong(module.getId()), StringField.TYPE_STORED));
			//https://stackoverflow.com/questions/29695307/sortiing-string-field-alphabetically-in-lucene-5-0
			doc.add(new SortedDocValuesField("moduleId", new BytesRef(PaddingUtil.paddingLong(module.getId()))));
			doc.add(new Field("name", ""+module.getName(), StringField.TYPE_STORED));
			doc.add(new Field("protocol", ""+module.getProtocol(), StringField.TYPE_STORED));
			doc.add(new Field("pluginConf", module.getPluginConf()==null?"":module.getPluginConf(), StringField.TYPE_STORED));
			doc.add(new Field("createTime", PaddingUtil.paddingLong(module.getCreateTime().getTime()), StringField.TYPE_STORED));
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

	public boolean deleteModule(int moduleId) {
		IndexWriter indexWriter = openIndexWriter(indexPath);
		if(indexWriter==null){
			return false;
		}
		try {
			Term term = new Term("moduleId",PaddingUtil.paddingLong(moduleId));
			indexWriter.deleteDocuments(term);
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
	
}
