package io.otdd.otddserver.service;

import io.otdd.otddserver.entity.TaskTarget;
import io.otdd.otddserver.util.PaddingUtil;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@EnableAutoConfiguration
public class TargetService extends BaseService{

    private String targetIndexPath = "targets";

    public List<TaskTarget> getActiveTargets() {
        List<TaskTarget> ret = new ArrayList<TaskTarget>();
        DirectoryReader reader = openDirectoryReader(targetIndexPath);
        if(reader==null){
            return ret;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            int MAX_RESULTS = 10000;
            MatchAllDocsQuery q = new MatchAllDocsQuery();
            SortField sf = new SortField("fullTarget", SortField.Type.STRING, false);
            Sort sort = new Sort(sf);
            TopDocs docs = searcher.search(q,1000,sort);
            if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
                for(int i=0;i<docs.scoreDocs.length;i++) {
                    Document doc = searcher.doc(docs.scoreDocs[i].doc);
                    ret.add(convertDocToTaskTarget(doc));
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

    public boolean createOrUpdateTaskTarget(String username, String tag, String mac) {
        String fullTarget = username + "." + tag + "." + mac;
        TaskTarget target = findTaskTarget(fullTarget);
        if(target==null){
            target = new TaskTarget();
            target.setUsername(username);
            target.setTag(tag);
            target.setMac(mac);
        }
        target.setLastActiveTime(new Date());

        IndexWriter indexWriter = openIndexWriter(targetIndexPath);
        if(indexWriter==null){
            return false;
        }
        try {
            //delete
            Term term = new Term("fullTarget",fullTarget);
            indexWriter.deleteDocuments(term);
            indexWriter.flush();

            //add
            Document doc = new Document();
            doc.add(new Field("fullTarget", fullTarget, StringField.TYPE_STORED));
            //https://stackoverflow.com/questions/29695307/sortiing-string-field-alphabetically-in-lucene-5-0
            doc.add(new SortedDocValuesField("fullTarget", new BytesRef(fullTarget)));
            doc.add(new Field("username", username, StringField.TYPE_STORED));
            doc.add(new Field("tag",tag, StringField.TYPE_STORED));
            doc.add(new Field("mac",mac, StringField.TYPE_STORED));
            doc.add(new Field("lastActiveTime", PaddingUtil.paddingLong(target.getLastActiveTime().getTime()), TextField.TYPE_STORED));
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

    private TaskTarget findTaskTarget(String fullTarget) {
        DirectoryReader reader = openDirectoryReader(targetIndexPath);
        if(reader==null){
            return null;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            Term term = new Term("fullTarget",fullTarget);
            Query query = new TermQuery(term);
            ScoreDoc[] hits = searcher.search(query, 1).scoreDocs;
            if(hits!=null&&hits.length>0){
                Document doc = searcher.doc(hits[0].doc);
                return convertDocToTaskTarget(doc);
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

    private TaskTarget convertDocToTaskTarget(Document doc) {
        if(doc==null){
            return null;
        }
        TaskTarget target = new TaskTarget();
        for(IndexableField field:doc.getFields()){
            if("username".equals(field.name())){
                target.setUsername(field.getCharSequenceValue().toString());
            }
            else if("tag".equals(field.name())){
                target.setTag(field.getCharSequenceValue().toString());
            }
            else if("mac".equals(field.name())){
                target.setMac(field.getCharSequenceValue().toString());
            }
            else if("lastActiveTime".equals(field.name())){
                target.setLastActiveTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
        }
        return target;
    }

}
