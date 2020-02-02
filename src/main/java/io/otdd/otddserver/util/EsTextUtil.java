package io.otdd.otddserver.util;

public class EsTextUtil {
	public static String getTextFromBytes(byte[] bytes){
		return TextUtil.removeNonPrintable(new String(bytes,0,bytes.length<10000?bytes.length:10000));
	}
}
