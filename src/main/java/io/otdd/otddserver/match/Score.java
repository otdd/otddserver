package io.otdd.otddserver.match;

import java.util.HashMap;
import java.util.Map;

public class Score {
	private double levenshteinDistance = 0.0f;
	private Map<String,Double> boosts = new HashMap<String,Double>();
	public double getLevenshteinDistance() {
		return levenshteinDistance;
	}
	public void setLevenshteinDistance(double levenshteinDistance) {
		this.levenshteinDistance = levenshteinDistance;
	}
	public Map<String, Double> getBoosts() {
		return boosts;
	}
	public void setBoosts(Map<String, Double> boosts) {
		this.boosts = boosts;
	}
	
	public double getTotalScore(){
		double total = levenshteinDistance;
		for(Double d: boosts.values()){
			total+=d;
		}
		return total;
	}
}
