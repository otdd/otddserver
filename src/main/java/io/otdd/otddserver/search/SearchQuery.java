package io.otdd.otddserver.search;

import lombok.Data;

import java.util.Date;

@Data
public class SearchQuery {

	private Date startTime;
	private Date endTime;
	private String testId;
	private int moduleId;
	private String inboundReq;
	private String inboundResp;
	private String outboundReq;
	private String outboundResp;
	private int curPage;
	private int pageSize;
	private String testStoreType = TestStoreType.ONLINE_RECORDED_TEST;

}
