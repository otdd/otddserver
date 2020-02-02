package io.otdd.otddserver.report;

import com.sksamuel.diffpatch.DiffMatchPatch;
import com.sksamuel.diffpatch.DiffMatchPatch.Diff;
import io.otdd.otddserver.edit.EditTest;
import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.testcase.OutboundCallBase;
import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.testcase.TestBase;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportUtil {
	public static void generateReport(TestBase test, TestResult result){
		DiffResult diff = getDiffResult(test.getInboundCall().getReqBytes(),
				result.getInboundCallResult().getReqBytes());
		result.getInboundCallResult().setReqDiff(diff);

		diff = getDiffResult(test.getInboundCall().getRespBytes(),
				result.getInboundCallResult().getRespBytes());
		result.getInboundCallResult().setRespDiff(diff);

		for(OutboundCallResult outboundResult:result.getOutboundCallResults()){
			if(outboundResult.getMatchedPeerIndex()>=0){
				OutboundCallBase matchedCall = test.getOutboundCalls().get(outboundResult.getMatchedPeerIndex());
				diff = getDiffResult(matchedCall.getReqBytes(),
						outboundResult.getReqBytes());
				outboundResult.setReqDiff(diff);

				diff = getDiffResult(matchedCall.getRespBytes(),
						outboundResult.getRespBytes());
				outboundResult.setRespDiff(diff);
			}
			else{
				outboundResult.setReqDiff(null);
				outboundResult.setRespDiff(null);
			}
		}
		if(test instanceof Test){
			result.setTestStoreType(TestStoreType.ONLINE_RECORDED_TEST);
		}
		else if(test instanceof EditTest){
			result.setTestStoreType(TestStoreType.EDITED_TEST);
		}
	}

	private static DiffResult getDiffResult(byte[] origin, byte[] match) {
		if(origin==null||match==null){
			return null;
		}
		DiffResult result = new DiffResult();
		DiffMatchPatch dmp = new DiffMatchPatch();
		LinkedList<Diff> diff = dmp.diff_main(new String(origin), new String(match));
		dmp.diff_cleanupSemantic(diff);

		List<Diff> originDiffs = diff.stream().filter(d -> d.operation != DiffMatchPatch.Operation.INSERT).collect(Collectors.toList());
		List<Diff> matchDiffs = diff.stream().filter(d -> d.operation != DiffMatchPatch.Operation.DELETE).collect(Collectors.toList());

		List<Diff> tmp = diff.stream().filter(d -> d.operation != DiffMatchPatch.Operation.EQUAL).collect(Collectors.toList());
		if(tmp==null||tmp.size()==0){
			result.setTheSame(true);
			return result;
		}

		result.setTheSame(false);
		result.setOriginContent(toPrettyHtml(originDiffs).getBytes());
		result.setMatchContent(toPrettyHtml(matchDiffs).getBytes());

		return result;
	}

	public static String toPrettyHtml(List<Diff> diffs){
		StringBuilder html = new StringBuilder();
		for (Diff aDiff : diffs) {
			String text = aDiff.text.replace("&", "&amp;").replace("<", "&lt;")
					.replace(">", "&gt;");
			switch (aDiff.operation) {
			case INSERT:
				html.append("<span class=\"diff\">").append(text)
				.append("</span>");
				break;
			case DELETE:
				html.append("<span class=\"diff\">").append(text)
				.append("</span>");
				break;
			case EQUAL:
				//				html.append("<span>").append(text).append("</span>");
				html.append(text);
				break;
			}
		}
		return html.toString();
	}

	public static void main_bak(String args[]){
		String origin = "This is a test senctence.";
		String match = "This is a test senctence.";

		DiffMatchPatch dmp = new DiffMatchPatch();
		LinkedList<Diff> diff = dmp.diff_main(origin, match);
		dmp.diff_cleanupSemantic(diff);

		List<Diff> baseDiffs = diff.stream().filter(d -> d.operation != DiffMatchPatch.Operation.INSERT).collect(Collectors.toList());
		List<Diff> testDiffs = diff.stream().filter(d -> d.operation != DiffMatchPatch.Operation.DELETE).collect(Collectors.toList());

		System.out.println(diff);

		System.out.println(toPrettyHtml(baseDiffs));
		System.out.println(toPrettyHtml(testDiffs));
	}
}
