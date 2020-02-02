package io.otdd.otddserver.service;

import io.otdd.otddserver.edit.EditTest;
import io.otdd.otddserver.search.SearchQuery;
import io.otdd.otddserver.search.SearchResult;
import io.otdd.otddserver.testcase.OutboundCallBase;
import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.util.PaddingUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@EnableAutoConfiguration
public class EditService extends TestBaseService {

    @Autowired
    TestService testService;

    private String indexPath = "editedcases";

    public SearchResult search(SearchQuery searchQuery){
        return search(indexPath,searchQuery);
    }

    public String newEdit(int moduleId,String testId) {

        Test test = testService.getTestById(moduleId,testId);
        if (test == null) {
            return null;
        } else {
            EditTest editTest = new EditTest(test);
            return this.createEditTest(editTest) ? editTest.getId() : null;
        }

    }

    private boolean createEditTest(EditTest testCase){
        return this.saveTest(indexPath,testCase);
    }

    public EditTest getEditTestById(String testId) {
        return (EditTest)this.getTestById(indexPath,testId,EditTest.class);
    }

    public boolean updateEditTest(EditTest editTest) {
        if(deleteEditTest(editTest.getId())){
            return false;
        }
        return this.saveTest(indexPath,editTest);
    }

    public boolean deleteEditTest(String id){
        return this.deleteTest(indexPath,id);
    }

}
