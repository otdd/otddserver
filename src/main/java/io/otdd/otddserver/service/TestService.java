package io.otdd.otddserver.service;

import io.otdd.otddserver.search.SearchQuery;
import io.otdd.otddserver.search.SearchResult;
import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.util.PaddingUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.springframework.stereotype.Service;

@Service
public class TestService extends TestBaseService{

    private String indexPath = "testcases";

    public SearchResult search(SearchQuery searchQuery){
        return search(indexPath+"/"+searchQuery.getModuleId(),searchQuery);
    }

    public void saveTest(Test testCase){
        this.saveTest(indexPath+"/"+testCase.getModuleId(),testCase);
    }

    public Test getTestById(int moduleId,String testId){
        return (Test)this.getTestById(indexPath+"/"+moduleId,testId,Test.class);
    }
}
