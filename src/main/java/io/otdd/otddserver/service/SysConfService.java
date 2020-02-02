package io.otdd.otddserver.service;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;

@Service
@EnableAutoConfiguration
public class SysConfService extends BaseService {

	private String indexPath = "sysconfig";

	public String getConfigByKey(String key){
		DirectoryReader reader = openDirectoryReader(indexPath);
		if(reader==null){
			return null;
		}
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			Term term = new Term("key",key);
			Query query = new TermQuery(term);
			ScoreDoc[] hits = searcher.search(query, 1).scoreDocs;
			if(hits!=null&&hits.length>0){
				Document doc = searcher.doc(hits[0].doc);
				return doc.getField("value").getCharSequenceValue().toString();
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

	public boolean saveConfigByKey(String key, String value) {
		IndexWriter indexWriter = openIndexWriter(indexPath);
		if(indexWriter==null){
			return false;
		}
		try {
			PhraseQuery q = new PhraseQuery("key", key);
			indexWriter.deleteDocuments(q);
			Document doc = new Document();
			doc.add(new Field("key", key, StringField.TYPE_STORED));
			doc.add(new Field("value", value, StringField.TYPE_STORED));
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
}
