package io.otdd.otddserver.vo;

import lombok.Data;

@Data
public class PageInfoVo {
	private int current=1;
	private int pageSize;
	private int total=0;
	
	public PageInfoVo(){
	}

	public PageInfoVo(int pageSize){
		this.pageSize = pageSize;
	}

}
