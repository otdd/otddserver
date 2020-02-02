package io.otdd.otddserver.match;

import org.apache.commons.text.beta.similarity.LevenshteinDistance;

import java.util.concurrent.Callable;

public class MatchTask implements Callable<Score>{

	private boolean LIMIT_LEN = true;
	private int LIMIT_MATCH_LEN = 2000;
	
	private byte[] baseBytes;
	private byte[] matchBytes;
	private MatchType matchType;
	
	public MatchTask(byte[] baseBytes, byte[] matchBytes, MatchType matchType){
		this.baseBytes = baseBytes;
		this.matchBytes = matchBytes;
		this.matchType = matchType;
	}
	
	@Override
	public Score call() throws Exception {
		String baseBytesStr = new String(baseBytes);
		String matchBytesStr = new String(matchBytes);
		Score score = new Score();
		if(matchType==MatchType.CONTAINS){
			if(matchBytesStr.contains(baseBytesStr)){
				score.setLevenshteinDistance(1);
			}
			return score;
		}
		else{
			int diffLen = Math.abs(baseBytes.length-matchBytes.length);
			int maxLen = Math.max(baseBytes.length, matchBytes.length);

			double diffLenRate = diffLen/(double)maxLen;
			if(diffLenRate>0.7){
				return null;
			}

			LevenshteinDistance distance = new LevenshteinDistance();
			int cnt = distance.apply(baseBytesStr.substring(0, LIMIT_LEN?Math.min(LIMIT_MATCH_LEN, baseBytesStr.length()):baseBytesStr.length()), 
					matchBytesStr.substring(0,LIMIT_LEN?Math.min(LIMIT_MATCH_LEN, matchBytesStr.length()):matchBytesStr.length()));

			maxLen = Math.max(baseBytesStr.length(), matchBytesStr.length());
			if(LIMIT_LEN&&maxLen>LIMIT_MATCH_LEN){
				maxLen = LIMIT_MATCH_LEN;
			}
			
			double levenshteinDistanceScore = new Double((double)(maxLen-cnt)/maxLen);
			score.setLevenshteinDistance(levenshteinDistanceScore);
			
			return score;
		}
	
	}

}
