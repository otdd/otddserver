package io.otdd.otddserver.vo;

import io.otdd.otddserver.report.DiffResult;
import lombok.Data;

@Data
public class DiffResultVo {

	private boolean isTheSame = true;
	private byte[] originContent;
	private byte[] matchContent;
	
	public DiffResultVo(){
		
	}
	public DiffResultVo(DiffResult diff) {
		if(diff==null){
			return;
		}
		this.isTheSame = diff.isTheSame();
		this.originContent = diff.getOriginContent();
		this.matchContent = diff.getMatchContent();
	}

}
