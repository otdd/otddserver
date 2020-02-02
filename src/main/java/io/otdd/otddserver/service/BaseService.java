package io.otdd.otddserver.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class BaseService {

    @Value("${pv.rootpath}")
    protected String pvRootPath;

    protected DirectoryReader openDirectoryReader(String path){
        try {
            Path indexPath = Paths.get(pvRootPath + (pvRootPath.endsWith("/")?"":"/") + "data" + (path.startsWith("/")?"":"/") + path);
            Directory directory = FSDirectory.open(indexPath);
            DirectoryReader reader = DirectoryReader.open(directory);
            return reader;
        }
        catch(IndexNotFoundException e){

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    protected IndexWriter openIndexWriter(String path) {
        try {
            IndexWriter writer = null;
            for(int i=0;i<10;i++){
                try {
                    Analyzer analyzer = new StandardAnalyzer();
//                    Analyzer analyzer = new WhitespaceAnalyzer();
                    IndexWriterConfig config = new IndexWriterConfig(analyzer);
                    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                    Path indexPath = Paths.get(pvRootPath + (pvRootPath.endsWith("/")?"":"/") + "data" + (path.startsWith("/")?"":"/") + path);
                    Directory directory = FSDirectory.open(indexPath);
                    writer = new IndexWriter(directory, config);
                    return writer;
                }
                catch (LockObtainFailedException e){
                    Random r = new Random();
                    Thread.sleep(20+r.nextInt(80));
                    continue;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
