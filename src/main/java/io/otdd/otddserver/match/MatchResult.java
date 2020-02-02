package io.otdd.otddserver.match;

public class MatchResult {
	
	private int matchedIndex = -1;
	private Score score = new Score();
	
	public int getMatchedIndex() {
		return matchedIndex;
	}
	public void setMatchedIndex(int matchedIndex) {
		this.matchedIndex = matchedIndex;
	}
	public Score getScore() {
		return score;
	}
	public void setScore(Score score) {
		this.score = score;
	}
	
}
