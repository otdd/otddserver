package io.otdd.otddserver.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult {

	private int totalNum;
	private int pageSize;
	private int curPage;
	private String testStoreType;
	private List<Object> tests = new ArrayList<Object>();

}
