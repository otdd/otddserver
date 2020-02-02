package io.otdd.otddserver.service;

import io.otdd.otddserver.entity.Log;
import io.otdd.otddserver.entity.TestLogList;
import io.otdd.otddserver.util.PaddingUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@EnableAutoConfiguration
public class LogService extends BaseService{

    private String indexPath = "logs";

    public TestLogList fetchLog(String testId,int runId,long timestamp,int cnt){
        TestLogList ret = new TestLogList();
        DirectoryReader reader = openDirectoryReader(indexPath);
        if(reader==null){
            return null;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            SortField sf = new SortField("logTime", SortField.Type.STRING, false);
            Sort sort = new Sort(sf);
            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            PhraseQuery q = new PhraseQuery("testId",testId);
            booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            q = new PhraseQuery("runId",PaddingUtil.paddingLong(runId));
            booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            //https://www.elastic.co/blog/apache-lucene-numeric-filters
            String lowerTerm = PaddingUtil.paddingLong(timestamp);
            TermRangeQuery rangeQuery = TermRangeQuery.newStringRange("logTime",
                        lowerTerm,"99999999999999",false,false);
            booleanQueryBuilder.add(rangeQuery,BooleanClause.Occur.MUST);
            TopDocs docs = searcher.search(booleanQueryBuilder.build(),cnt,sort);
            if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
                for(int i=0;i<docs.scoreDocs.length;i++) {
                    Document doc = searcher.doc(docs.scoreDocs[i].doc);
                    Log log = convertDocToLog(doc);
                    ret.getLogs().add(log);
                    ret.setPulledTimestamp(log.getLogTime());
                }
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
        return ret;

    }

    private Log convertDocToLog(Document doc) {
        if(doc==null){
            return null;
        }
        Log log = new Log();
        for(IndexableField field:doc.getFields()){
            if("testId".equals(field.name())){
                log.setTestId(field.getCharSequenceValue().toString());
            }
            else if("runId".equals(field.name())){
                log.setRunId(Integer.parseInt(field.getCharSequenceValue().toString()));
            }
            else if("logTime".equals(field.name())){
                log.setLogTime(Long.parseLong(field.getCharSequenceValue().toString()));
            }
            else if("level".equals(field.name())){
                log.setLevel(field.getCharSequenceValue().toString());
            }
            else if("log".equals(field.name())){
                log.setLog(field.getCharSequenceValue().toString());
            }
        }
        return log;
    }

    public void log(String testId, int runId, String log,long timestamp,String level) {
        IndexWriter indexWriter = openIndexWriter(indexPath);
        if(indexWriter==null){
            return;
        }
        try {
            Document doc = new Document();
            doc.add(new Field("testId", testId, StringField.TYPE_STORED));
            doc.add(new Field("runId", PaddingUtil.paddingLong(runId), StringField.TYPE_STORED));
            doc.add(new Field("logTime", PaddingUtil.paddingLong(timestamp), StringField.TYPE_STORED));
            //https://stackoverflow.com/questions/29695307/sortiing-string-field-alphabetically-in-lucene-5-0
            doc.add(new SortedDocValuesField("logTime", new BytesRef(PaddingUtil.paddingLong(timestamp))));
            doc.add(new Field("log", log, StringField.TYPE_STORED));
            doc.add(new Field("level", level, StringField.TYPE_STORED));
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

}
