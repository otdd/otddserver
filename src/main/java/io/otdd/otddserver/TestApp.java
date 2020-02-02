package io.otdd.otddserver;
import io.grpc.*;
import io.otdd.otddserver.grpc.OtddServerServiceImpl;
import io.otdd.otddserver.grpc.TestRunnerServiceImpl;
import io.otdd.otddserver.util.PaddingUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.springframework.util.Base64Utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

public class TestApp {
    public static void main_bak( String[] args ) throws Exception
    {


        Analyzer analyzer = new StandardAnalyzer();

        Path indexPath = Files.createTempDirectory("tempIndex");
        Directory directory = FSDirectory.open(indexPath);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        Document doc = new Document();
        String text = "GET \t\t  HEHE SS";
        doc.add(new TextField("inbound.reqText", text, Field.Store.YES));
        doc.add(new Field("inbound.reqTime", "2", TextField.TYPE_STORED));
        doc.add(new SortedDocValuesField("inbound.reqTime", new BytesRef("2")));
        iwriter.addDocument(doc);
        iwriter.close();

        // Now search the index:
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        // Parse a simple query that searches for "text":
        QueryParser parser = new QueryParser("inbound.reqText", analyzer);
        Query q = parser.parse("\"GET HEHE\"");
//        PhraseQuery.Builder builder = new PhraseQuery.Builder();
//        builder.add(new Term("inbound.reqText", "HEHE"), 0);
//        PhraseQuery q = new PhraseQuery("inbound.reqText","HEHE");

        // Iterate through the results:
//        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
//        booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
//        MatchAllDocsQuery qq = new MatchAllDocsQuery();
//        booleanQueryBuilder.add(qq,BooleanClause.Occur.MUST);

        TopDocs t = isearcher.search(q,10);
        SortField sf = new SortField("inbound.reqTime", SortField.Type.STRING, true);
        Sort sort = new Sort(sf);
        TopFieldCollector collector = TopFieldCollector.create(sort,40,1000);
        isearcher.search(q, collector);
        TopDocs hits = collector.topDocs(0,10);
//        for (int i = 0; i < hits.length; i++) {
//            Document hitDoc = isearcher.doc(hits[i].doc);
//            System.out.println(hitDoc);
//        }
        ireader.close();
        directory.close();
        IOUtils.rm(indexPath);

        /*

        System.out.println(Base64Utils.encodeToString("GET /reviews/0 HTTP/1.1\r\n\r\n".getBytes()));
        System.out.println(Base64Utils.encodeToString("HTTP/1.1 200 OK\r\nX-Powered-By: Servlet/3.1\r\nContent-Type: application/json\r\nDate: Sat, 18 Jan 2020 06:18:17 GMT\r\nContent-Language: en-US\r\nContent-Length: 379\r\n\r\n{\"id\": \"0\",\"reviews\": [{  \"reviewer\": \"Reviewer1\",  \"text\": \"An extremely entertaining play by Shakespeare. The slapstick humour is refreshing!\", \"rating\": {\"stars\": 2, \"color\": \"black\"}},{  \"reviewer\": \"Reviewer2\",  \"text\": \"Absolutely fun and entertaining. The play lacks thematic depth when compared to other plays by Shakespeare.\", \"rating\": {\"stars\": 4, \"color\": \"black\"}}]}".getBytes()));
        System.out.println(Base64Utils.encodeToString(("GET /ratings/0 HTTP/1.1\r\n" +
                "Accept: application/json\r\n" +
                "User-Agent: Apache-CXF/3.1.18\r\n" +
                "Cache-Control: no-cache\r\n" +
                "Pragma: no-cache\r\n" +
                "Host: 127.0.0.1:9080\r\n" +
                "Connection: keep-alive\r\n\r\n").getBytes()));
        System.out.println(Base64Utils.encodeToString("HTTP/1.1 200 OK\r\ncontent-length: 48\r\n\r\n{\"id\":0,\"ratings\":{\"Reviewer1\":2,\"Reviewer2\":4}}".getBytes()));

        if(true){
            return;
        }
        // Create a new server to listen on port 8080
        Server server = ServerBuilder.forPort(8764)
                .addService(new TestRunnerServiceImpl())
                .addService(new OtddServerServiceImpl())
                .build();

        // Start the server
        server.start();

        // Server threads are running in the background.
        System.out.println("Server started");
        // Don't exit the main thread. Wait until server is terminated.
        server.awaitTermination();
        */
    }
}
