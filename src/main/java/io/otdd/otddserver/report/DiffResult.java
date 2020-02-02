package io.otdd.otddserver.report;

import lombok.Data;

@Data
public class DiffResult {
	private boolean isTheSame = true;
	private byte[] originContent;
	private byte[] matchContent;
}
