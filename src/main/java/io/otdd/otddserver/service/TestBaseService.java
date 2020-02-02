package io.otdd.otddserver.service;

import io.otdd.otddserver.edit.EditInboundCall;
import io.otdd.otddserver.edit.EditOutboundCall;
import io.otdd.otddserver.edit.EditTest;
import io.otdd.otddserver.match.MatchType;
import io.otdd.otddserver.search.SearchQuery;
import io.otdd.otddserver.search.SearchResult;
import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.testcase.*;
import io.otdd.otddserver.util.PaddingUtil;
import io.otdd.otddserver.util.TextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TestBaseService extends BaseService{

    protected SearchResult search(String indexPath,SearchQuery searchQuery) {
        SearchResult result = new SearchResult();
        result.setCurPage(searchQuery.getCurPage());
        result.setTestStoreType(searchQuery.getTestStoreType());
        result.setPageSize(searchQuery.getPageSize());

        DirectoryReader reader = openDirectoryReader(indexPath);
        if(reader==null){
            return null;
        }
        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            int MAX_RESULTS = 1000000;
//            TopScoreDocCollector collector = TopScoreDocCollector.create(searchQuery.getPageSize(),MAX_RESULTS);  // MAX_RESULTS is just an int limiting the total number of hits
            SortField sf = new SortField("inbound.reqTime", SortField.Type.STRING, true);
            Sort sort = new Sort(sf);
            int start = searchQuery.getPageSize()*(searchQuery.getCurPage()-1);
            TopFieldCollector collector = TopFieldCollector.create(sort,start+searchQuery.getPageSize(),MAX_RESULTS);
            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            Analyzer analyzer = new StandardAnalyzer();
            if(!StringUtils.isBlank(searchQuery.getInboundReq())){
                QueryParser parser = new QueryParser("inbound.reqText", analyzer);
                Query q = parser.parse(searchQuery.getInboundReq());
                booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            }
            if(!StringUtils.isBlank(searchQuery.getInboundResp())){
                QueryParser parser = new QueryParser("inbound.respText", analyzer);
                Query q = parser.parse(searchQuery.getInboundResp());
                booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            }
            if(!StringUtils.isBlank(searchQuery.getOutboundReq())){
                QueryParser parser = new QueryParser("outbound.reqText", analyzer);
                Query q = parser.parse(searchQuery.getOutboundReq());
                booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            }
            if(!StringUtils.isBlank(searchQuery.getOutboundResp())){
                QueryParser parser = new QueryParser("outbound.respText", analyzer);
                Query q = parser.parse(searchQuery.getOutboundResp());
                booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            }
            if(!StringUtils.isBlank(searchQuery.getTestId())){
                Term t = new Term("testId",searchQuery.getTestId());
                TermQuery q = new TermQuery(t);
                booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            }

            //https://www.elastic.co/blog/apache-lucene-numeric-filters
            String lowerTerm = null;
            String upperTerm = null;
            if(searchQuery.getStartTime()!=null){
                lowerTerm = PaddingUtil.paddingLong(searchQuery.getStartTime().getTime());
            }
            if(searchQuery.getEndTime()!=null){
                upperTerm = PaddingUtil.paddingLong(searchQuery.getEndTime().getTime());
            }
            if(lowerTerm!=null||upperTerm!=null){
                TermRangeQuery rangeQuery = TermRangeQuery.newStringRange("inbound.reqTime",
                        lowerTerm==null?"00000000000000":lowerTerm,
                        upperTerm==null?"99999999999999":upperTerm,true,true);
                booleanQueryBuilder.add(rangeQuery,BooleanClause.Occur.MUST);
            }
            MatchAllDocsQuery q = new MatchAllDocsQuery();
            booleanQueryBuilder.add(q,BooleanClause.Occur.MUST);
            searcher.search(booleanQueryBuilder.build(), collector);
            TopDocs docs = collector.topDocs(start,searchQuery.getPageSize());
            result.setTotalNum((int)docs.totalHits.value);
            if(docs.scoreDocs!=null&&docs.scoreDocs.length>0){
                for(int i=0;i<docs.scoreDocs.length;i++) {
                    Document doc = searcher.doc(docs.scoreDocs[i].doc);
                    if(TestStoreType.EDITED_TEST.equalsIgnoreCase(searchQuery.getTestStoreType())) {
                        result.getTests().add(convertDocToTest(doc, EditTest.class));
                    }
                    else{
                        result.getTests().add(convertDocToTest(doc, Test.class));
                    }
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

        return result;
    }

    protected TestBase getTestById(String indexPath, String testId,Class type) {

        DirectoryReader reader = openDirectoryReader(indexPath);
        if(reader==null){
            return null;
        }
        try {
            Analyzer analyzer = new StandardAnalyzer();
            IndexSearcher searcher = new IndexSearcher(reader);
            Term term = new Term("testId",testId);
            Query query = new TermQuery(term);
            ScoreDoc[] hits = searcher.search(query, 1).scoreDocs;
            if(hits!=null&&hits.length>0){
                Document doc = searcher.doc(hits[0].doc);
                return convertDocToTest(doc,type);
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

    protected TestBase convertDocToTest(Document doc,Class type) {
        TestBase test = null;
        if(EditTest.class.equals(type)){
            test = new EditTest();
        }
        else{
            test = new Test();
        }
        Pattern outboundReqPattern = Pattern.compile("outbound\\.([0-9]+)\\.reqBytes");
        Pattern outboundReqTimePattern = Pattern.compile("outbound\\.([0-9]+)\\.reqTime");
        Pattern outboundRespPattern = Pattern.compile("outbound\\.([0-9]+)\\.respBytes");
        Pattern outboundRespTimePattern = Pattern.compile("outbound\\.([0-9]+)\\.respTime");
        Pattern outboundFromIndexPattern = Pattern.compile("outbound\\.([0-9]+)\\.fromIndex");
        Pattern outboundMatchTypePattern = Pattern.compile("outbound\\.([0-9]+)\\.matchType");
        Pattern outboundProtocolPattern = Pattern.compile("outbound\\.([0-9]+)\\.protocol");

        InboundCallBase inboundCall = null;
        Map<Integer, OutboundCallBase> outboundCalls = new TreeMap<>();

        for(IndexableField field:doc.getFields()){
            Matcher outboundReqMatcher = outboundReqPattern.matcher(field.name());
            Matcher outboundReqTimeMatcher = outboundReqTimePattern.matcher(field.name());
            Matcher outboundRespMatcher = outboundRespPattern.matcher(field.name());
            Matcher outboundRespTimeMatcher = outboundRespTimePattern.matcher(field.name());
            Matcher outboundFromIndexMatcher = outboundFromIndexPattern.matcher(field.name());
            Matcher outboundMatchTypeMatcher = outboundMatchTypePattern.matcher(field.name());
            Matcher outboundProtocolMatcher = outboundProtocolPattern.matcher(field.name());

            if("testId".equals(field.name())){
                test.setId(field.getCharSequenceValue().toString());
            }
            if("fromId".equals(field.name())){
                if(EditTest.class.equals(type)){
                    ((EditTest)test).setFromId(field.getCharSequenceValue().toString());
                }
            }
            else if("moduleName".equals(field.name())){
                test.setModuleName(field.getCharSequenceValue().toString());
            }
            else if("moduleId".equals(field.name())){
                test.setModuleId(Integer.parseInt(field.getCharSequenceValue().toString()));
            }
            else if("insertTime".equals(field.name())){
                test.setInsertTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
            else if("inbound.reqBytes".equals(field.name())){
                if(inboundCall==null){
                    if(EditTest.class.equals(type)) {
                        inboundCall = new EditInboundCall();
                    }
                    else{
                        inboundCall = new InboundCall();
                    }
                }
                inboundCall.setReqBytes(field.binaryValue().bytes);
                inboundCall.setReqText(TextUtil.removeNonPrintable(new String(inboundCall.getReqBytes())));
            }
            else if("inbound.reqTime".equals(field.name())){
                if(inboundCall==null){
                    if(EditTest.class.equals(type)) {
                        inboundCall = new EditInboundCall();
                    }
                    else{
                        inboundCall = new InboundCall();
                    }
                }
                inboundCall.setReqTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
            else if("inbound.respBytes".equals(field.name())){
                if(inboundCall==null){
                    if(EditTest.class.equals(type)) {
                        inboundCall = new EditInboundCall();
                    }
                    else{
                        inboundCall = new InboundCall();
                    }
                }
                inboundCall.setRespBytes(field.binaryValue().bytes);
                inboundCall.setRespText(TextUtil.removeNonPrintable(new String(inboundCall.getRespBytes())));
            }
            else if("inbound.respTime".equals(field.name())) {
                if(inboundCall==null){
                    if(EditTest.class.equals(type)) {
                        inboundCall = new EditInboundCall();
                    }
                    else{
                        inboundCall = new InboundCall();
                    }
                }
                inboundCall.setRespTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
            else if(outboundReqMatcher.matches()){
                Integer index = Integer.parseInt(outboundReqMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    if(EditTest.class.equals(type)) {
                        outboundCalls.put(index, new EditOutboundCall());
                    }
                    else{
                        outboundCalls.put(index, new OutboundCall());
                    }
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setReqBytes(field.binaryValue().bytes);
                outboundCalls.get(index).setReqText(TextUtil.removeNonPrintable(new String(outboundCalls.get(index).getReqBytes())));
            }
            else if(outboundReqTimeMatcher.matches()){
                Integer index = Integer.parseInt(outboundReqTimeMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    if(EditTest.class.equals(type)) {
                        outboundCalls.put(index, new EditOutboundCall());
                    }
                    else{
                        outboundCalls.put(index, new OutboundCall());
                    }
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setReqTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
            else if(outboundRespMatcher.matches()){
                Integer index = Integer.parseInt(outboundRespMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    if(EditTest.class.equals(type)) {
                        outboundCalls.put(index, new EditOutboundCall());
                    }
                    else{
                        outboundCalls.put(index, new OutboundCall());
                    }
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setRespBytes(field.binaryValue().bytes);
                outboundCalls.get(index).setRespText(TextUtil.removeNonPrintable(new String(outboundCalls.get(index).getRespBytes())));
            }
            else if(outboundRespTimeMatcher.matches()){
                Integer index = Integer.parseInt(outboundRespTimeMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    if(EditTest.class.equals(type)) {
                        outboundCalls.put(index, new EditOutboundCall());
                    }
                    else{
                        outboundCalls.put(index, new OutboundCall());
                    }
                }
                outboundCalls.get(index).setIndex(index);
                outboundCalls.get(index).setRespTime(new Date(Long.parseLong(field.getCharSequenceValue().toString())));
            }
            else if(outboundFromIndexMatcher.matches()){
                Integer index = Integer.parseInt(outboundFromIndexMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    if(EditTest.class.equals(type)) {
                        outboundCalls.put(index, new EditOutboundCall());
                    }
                    else{
                        outboundCalls.put(index, new OutboundCall());
                    }
                }
                outboundCalls.get(index).setIndex(index);
                ((EditOutboundCall)outboundCalls.get(index)).setFromIndex(Integer.parseInt(field.getCharSequenceValue().toString()));
            }
            else if(outboundMatchTypeMatcher.matches()){
                Integer index = Integer.parseInt(outboundMatchTypeMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    if(EditTest.class.equals(type)) {
                        outboundCalls.put(index, new EditOutboundCall());
                    }
                    else{
                        outboundCalls.put(index, new OutboundCall());
                    }
                }
                outboundCalls.get(index).setIndex(index);
                ((EditOutboundCall)outboundCalls.get(index)).setMatchType(MatchType.values()[Integer.parseInt(field.getCharSequenceValue().toString())]);
            }
            else if(outboundProtocolMatcher.matches()){
                Integer index = Integer.parseInt(outboundProtocolMatcher.group(1));
                if(outboundCalls.get(index)==null){
                    if(EditTest.class.equals(type)) {
                        outboundCalls.put(index, new EditOutboundCall());
                    }
                    else{
                        outboundCalls.put(index, new OutboundCall());
                    }
                }
                outboundCalls.get(index).setIndex(index);
                ((EditOutboundCall)outboundCalls.get(index)).setProtocol(field.getCharSequenceValue().toString());
            }
        }
        test.setInboundCall(inboundCall);
        for(OutboundCallBase b:outboundCalls.values()){
            test.getOutboundCalls().add(b);
        }
        return test;
    }

    protected boolean saveTest(String indexPath, TestBase testCase) {
        IndexWriter indexWriter = openIndexWriter(indexPath);
        if(indexWriter==null){
            return false;
        }
        try {
            Document doc = new Document();
            doc.add(new Field("testId", testCase.getId(), StringField.TYPE_STORED));
            if(testCase instanceof EditTest){
                doc.add(new Field("fromId", ((EditTest)testCase).getFromId(), StringField.TYPE_STORED));
            }
            doc.add(new Field("moduleId", ""+testCase.getModuleId(), StringField.TYPE_STORED));
            doc.add(new Field("moduleName", testCase.getModuleName(), StringField.TYPE_STORED));
            doc.add(new Field("insertTime", ""+new Date().getTime(), StringField.TYPE_STORED));
            if(testCase.getInboundCall()!=null) {
                if(!StringUtils.isBlank(testCase.getInboundCall().getReqText())) {
                    doc.add(new Field("inbound.reqText", testCase.getInboundCall().getReqText(), TextField.TYPE_STORED));
                }
                if(testCase.getInboundCall().getReqBytes()!=null&&testCase.getInboundCall().getReqBytes().length>0) {
                    doc.add(new Field("inbound.reqBytes", testCase.getInboundCall().getReqBytes(), StoredField.TYPE));
                }
                if(testCase.getInboundCall().getReqTime()!=null){
                    doc.add(new Field("inbound.reqTime", PaddingUtil.paddingLong(testCase.getInboundCall().getReqTime().getTime()), TextField.TYPE_STORED));
                    doc.add(new SortedDocValuesField("inbound.reqTime", new BytesRef(PaddingUtil.paddingLong(testCase.getInboundCall().getReqTime().getTime()))));
                }
                if(!StringUtils.isBlank(testCase.getInboundCall().getRespText())) {
                    doc.add(new Field("inbound.respText", testCase.getInboundCall().getRespText(), TextField.TYPE_STORED));
                }
                if(testCase.getInboundCall().getRespBytes()!=null&&testCase.getInboundCall().getRespBytes().length>0) {
                    doc.add(new Field("inbound.respBytes", testCase.getInboundCall().getRespBytes(), StoredField.TYPE));
                }
                if(testCase.getInboundCall().getRespTime()!=null){
                    doc.add(new Field("inbound.respTime", PaddingUtil.paddingLong(testCase.getInboundCall().getRespTime().getTime()), TextField.TYPE_STORED));
                }
            }
            if(testCase.getOutboundCalls()!=null&&testCase.getOutboundCalls().size()>0){
                StringBuilder reqText = new StringBuilder();
                StringBuilder respText = new StringBuilder();
                for(int i=0;i<testCase.getOutboundCalls().size();i++){
                    OutboundCallBase outboundCall = testCase.getOutboundCalls().get(i);
                    if(!StringUtils.isBlank(outboundCall.getReqText())) {
                        reqText.append(outboundCall.getReqText() + " ");
                    }
                    if(!StringUtils.isBlank(outboundCall.getRespText())) {
                        respText.append(outboundCall.getRespText() + " ");
                    }
                    if(outboundCall.getReqBytes()!=null) {
                        doc.add(new Field("outbound." + i + ".reqBytes", outboundCall.getReqBytes(), StoredField.TYPE));
                    }
                    if(outboundCall.getReqTime()!=null){
                        doc.add(new Field("outbound." + i + ".reqTime", PaddingUtil.paddingLong(outboundCall.getReqTime().getTime()), TextField.TYPE_STORED));
                    }
                    if(outboundCall.getRespBytes()!=null) {
                        doc.add(new Field("outbound." + i + ".respBytes", outboundCall.getRespBytes(), StoredField.TYPE));
                    }
                    if(outboundCall.getRespTime()!=null){
                        doc.add(new Field("outbound." + i + ".respTime", PaddingUtil.paddingLong(outboundCall.getRespTime().getTime()), TextField.TYPE_STORED));
                    }
                    if(outboundCall instanceof EditOutboundCall){
                        doc.add(new Field("outbound." + i + ".fromIndex", PaddingUtil.paddingLong(((EditOutboundCall)outboundCall).getFromIndex()), StringField.TYPE_STORED));
                        doc.add(new Field("outbound." + i + ".matchType", PaddingUtil.paddingLong(((EditOutboundCall)outboundCall).getMatchType().ordinal()), StringField.TYPE_STORED));
                        if(((EditOutboundCall)outboundCall).getProtocol()!=null) {
                            doc.add(new Field("outbound." + i + ".protocol", ((EditOutboundCall) outboundCall).getProtocol(), StringField.TYPE_STORED));
                        }
                    }
                }
                doc.add(new Field("outbound.reqText", reqText, TextField.TYPE_STORED));
                doc.add(new Field("outbound.respText", respText, TextField.TYPE_STORED));
            }
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

    protected boolean deleteTest(String indexPath, String testId) {
        IndexWriter indexWriter = openIndexWriter(indexPath);
        if(indexWriter==null){
            return false;
        }
        try {
            TermQuery q = new TermQuery(new Term("testId", testId));
            indexWriter.deleteDocuments(q);
            indexWriter.flush();
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
