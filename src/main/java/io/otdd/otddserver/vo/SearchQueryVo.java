package io.otdd.otddserver.vo;

import lombok.Data;

@Data
public class SearchQueryVo {
	private String startTime;
	private String endTime;
	private String testId;
	private String inboundReq;
	private String inboundResp;
	private String outboundReq;
	private String outboundResp;
	private int moduleId = 0;
	private String testStoreType;
	
	private PageInfoVo pageInfo = new PageInfoVo(40);

}
