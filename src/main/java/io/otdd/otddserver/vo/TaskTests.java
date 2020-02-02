package io.otdd.otddserver.vo;

import io.otdd.otddserver.search.TestStoreType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskTests {

	public static int TYPE_FROM_IDS = 0;
	public static int TYPE_FROM_QUERY = 1;

	private int type = 0;
	private List<String> testIds = new ArrayList<String>();
	private SearchQueryVo query = new SearchQueryVo();
	private String testStoreType = TestStoreType.ONLINE_RECORDED_TEST;
	
}
