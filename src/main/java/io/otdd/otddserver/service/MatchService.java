package io.otdd.otddserver.service;

import io.otdd.otddserver.edit.EditOutboundCall;
import io.otdd.otddserver.edit.EditTest;
import io.otdd.otddserver.match.MatchResult;
import io.otdd.otddserver.match.MatchTask;
import io.otdd.otddserver.match.MatchType;
import io.otdd.otddserver.match.Score;
import io.otdd.otddserver.testcase.OutboundCallBase;
import io.otdd.otddserver.testcase.Test;
import io.otdd.otddserver.testcase.TestBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class MatchService {
	
	private ExecutorService executorService = Executors.newWorkStealingPool();

	public MatchResult getMostMatchedIndex(
            TestBase test, byte[] matchBytes){
		List<Future> futures = new ArrayList<Future>();
		for(OutboundCallBase outboundCall:test.getOutboundCalls()){
            MatchType matchType = MatchType.SIMILARITY;
		    if(test instanceof EditTest){
                MatchType tmp = ((EditOutboundCall)outboundCall).getMatchType();
		        if(tmp!=null){
                    matchType = tmp;
                }
            }
			MatchTask task = new MatchTask(outboundCall.getReqBytes(),matchBytes,matchType);
			Future<Score> future = executorService.submit(task);
			futures.add(future);
		}
		
		MatchResult result = new MatchResult();
		int index = 0;
		for(Future<Score> future:futures){
			try {
				Score score = future.get();
				if(score!=null&&
						score.getTotalScore()>result.getScore().getTotalScore()){
					result.setMatchedIndex(index);
					result.setScore(score);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			index++;
		}
		
		double minScore = getMinScore(matchBytes.length);
		if(result.getScore().getTotalScore()<minScore){
			return null;
		}
		return result;
	}

	private double getMinScore(int length) {
		double x1 = 50;
		double x2 = 600;
		double y1 = 0.3;
		double y2 = 0.6;
		if(length<x1){
			return y1;
		}
		if(length>x2){
			return y2;
		}
		double y = ((y2-y1)/(x2-x1))*length + (y1*x2-x1*y2)/(x2-x1);
		return y;
	}
	
}
