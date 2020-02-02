package io.otdd.otddserver.ddl;

public class ParseFromDDLResult {
	public enum Code{
		SUCCEED,
		FAILED,
		NO_CODEC
	}
	
	byte[] bytes;
	private Code errCode = Code.SUCCEED;
	private String errMsg = "";
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	public Code getErrCode() {
		return errCode;
	}
	public void setErrCode(Code errCode) {
		this.errCode = errCode;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
}
