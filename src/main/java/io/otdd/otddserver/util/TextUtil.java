package io.otdd.otddserver.util;

public class TextUtil {
	public static String removeNonPrintable(String src) {
		if (src == null) {
            return "";
        }
        return src.replaceAll("[^\\P{Cc}\\t\\r\\n]", "");
	}
}
