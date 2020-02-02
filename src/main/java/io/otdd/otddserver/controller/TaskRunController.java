package io.otdd.otddserver.controller;

import com.google.gson.Gson;
import io.otdd.otddserver.entity.*;
import io.otdd.otddserver.report.ReportUtil;
import io.otdd.otddserver.report.TestResult;
import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.service.*;
import io.otdd.otddserver.testcase.TestBase;
import io.otdd.otddserver.vo.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@EnableAutoConfiguration
@RequestMapping("/otdd/taskrun")
public class TaskRunController extends BaseController{

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRunService taskRunService;

	@Autowired
	private TargetService targetService;

	@Autowired
	private TestService testService;

	@Autowired
	private EditService editService;

	@Autowired
	private RunTestService runTestService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private LogService logService;

	@RequestMapping(value = "/getActiveTargets")
	@ResponseBody
	public Map<String, Object> getActiveTargets(@RequestBody String body) {
		List<TaskTarget> targets = targetService.getActiveTargets();
		if(targets!=null){
			List<String> vos = new ArrayList<String>();
			for(TaskTarget t:targets){
				String vo = t.getUsername()+"."+t.getTag()+"."+t.getMac();
				vos.add(vo);
			}
			return success(vos);
		}
		return fail();
	}

	@RequestMapping(value = "/newTask")
	@ResponseBody
	public Map<String, Object> newTask(@RequestBody String body) {
		Gson gson = new Gson();
		TaskVo taskVo = gson.fromJson(body, TaskVo.class);
		if("no".equalsIgnoreCase(taskVo.getConfig().getMockOutboundCalls())){
			taskVo.getConfig().setPassthoughConnections("");
		}
		int id = taskService.newTask(taskVo.getModuleId(),taskVo.getTarget(),
				taskVo.getTargetPort(),
				gson.toJson(taskVo.getConfig()),
				taskVo.getTests());
		if(id>0){
			return success(id);
		}
		else{
			return fail();
		}
	}

	@RequestMapping(value = "/getLastTaskConfig")
	@ResponseBody
	public Map<String, Object> getLastTaskConfig(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		int moduleId = json.getInt("moduleId");
		Task task = taskService.getLastTask(moduleId);
		if(task!=null){
			TaskVo vo = new TaskVo(task);
			Map<String,Object> ret = new HashMap<String,Object>();
			ret.put("config",vo.getConfig());
			ret.put("target",vo.getTarget());
			ret.put("targetPort",vo.getTargetPort());
			return success(ret);
		}
		return fail();
	}

	@RequestMapping(value = "/getTask")
	@ResponseBody
	public Map<String, Object> getTask(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		int taskId = json.getInt("id");
		Task task = taskService.getTask(taskId);
		if(task!=null){
			return success(new TaskVo(task));
		}
		else{
			return fail();
		}
	}
	
	@RequestMapping(value = "/getTaskList")
	@ResponseBody
	public Map<String, Object> getTaskList(@RequestBody String body) {
		Gson gson = new Gson();
		TaskListQueryVo vo = gson.fromJson(body, TaskListQueryVo.class);
		PageBean<Task> list = taskService.getTaskList(vo.getModuleId(),
				vo.getPageInfo().getCurrent(),vo.getPageInfo().getPageSize());
		if(list!=null){
			TaskListVo ret = new TaskListVo(list);
			return success(ret);
		}
		else{
			return fail();
		}
		
	}

	@RequestMapping(value = "/rerun")
	@ResponseBody
	public Map<String, Object> rerun(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		int taskId = json.getInt("id");
		if(taskService.rerun(taskId)){
			return success();
		}
		else{
			return fail();
		}
	}

	@RequestMapping(value = "/getTaskRunList")
	@ResponseBody
	public Map<String, Object> getTaskRunList(@RequestBody String body) {
		Gson gson = new Gson();
		TaskRunListQueryVo vo = gson.fromJson(body, TaskRunListQueryVo.class);
		TaskRunListQuery query = convertTaskRunListQueryVo(vo);
		PageBean<TaskRun> list = taskRunService.getTaskRunList(query);
		if(list!=null){
			PageBeanVo<TaskRunVo> ret = new PageBeanVo<TaskRunVo>(list);
			for(TaskRun t:list.getData()){
				ret.getData().add(new TaskRunVo(t));
			}
			return success(ret);
		}
		else{
			return fail();
		}
	}

	@RequestMapping(value = "/getRunProgress")
	@ResponseBody
	public Map<String, Object> getRunProgress(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		String runIds = json.getString("runIds");
		String[] ids = runIds.split(",");
		Map<Integer, RunProgress> ret = new HashMap<Integer,RunProgress>();
		for(String id:ids){
			RunProgress progress = runTestService.getRunProgress(Integer.parseInt(id));
			if(progress!=null){
				ret.put(progress.getRunId(),progress);
			}
		}
		return success(ret);
	}

	@RequestMapping(value = "/getReportedTestList")
	@ResponseBody
	public Map<String, Object> getReportedTestList(@RequestBody String body) {
		Gson gson = new Gson();
		ReportedTestListVo vo = gson.fromJson(body, ReportedTestListVo.class);
		PageBean<ReportedTest> list = reportService.getTaskRunReportList(vo.getRunId(),vo.getPageInfo().getCurrent(),vo.getPageInfo().getPageSize());
		if(list!=null){
		    TaskRun run = taskRunService.getById(vo.getRunId());
		    if(TaskRun.ENDED.equalsIgnoreCase(run.getStatus())){
		        vo.setCompleted(true);
            }
			vo.getPageInfo().setTotal(list.getTotalNum());
			vo.getPageInfo().setCurrent(list.getCurPage());
			vo.getPageInfo().setPageSize(list.getPageSize());
			vo.setReportedTests(list.getData());
			return success(vo);
		}
		return fail();
	}

	@RequestMapping(value = "/pullTestLog")
	@ResponseBody
	public Map<String, Object> pullTestLog(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		int runId = json.getInt("runId");
		String testId = json.getString("testId");
		long pulledTimestamp = json.getLong("pulledTimestamp");

		TestLogList list = logService.fetchLog(testId,
				runId,pulledTimestamp,5);
		if(list!=null){
			DateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			for(Log log:list.getLogs()){
				log.setLog(fm.format(new Date(log.getLogTime()))+" "+log.getLog());
			}
			return success(list);
		}
		return fail();
	}

	@RequestMapping(value = "/getTestReport")
	@ResponseBody
	public Map<String, Object> getTestReport(@RequestBody String body) {
		JSONObject json = JSONObject.fromObject(body);
		int runId = json.getInt("runId");
		String testId = json.getString("testId");
		TestResult result = reportService.getTestResult(runId,testId);
		if(result!=null){
            TaskRun run = taskRunService.getById(runId);
			TestBase test = null;
			if(TestStoreType.ONLINE_RECORDED_TEST.equals(result.getTestStoreType())){
				test = testService.getTestById(run.getModuleId(),testId);
			}
			else if(TestStoreType.EDITED_TEST.equals(result.getTestStoreType())){
				test = editService.getEditTestById(testId);
			}
			if(test!=null){
                ReportUtil.generateReport(test,result);
				TestVo testVo = new TestVo(test);
				TestResultVo resultVo = new TestResultVo(result);
				TestReportVo ret = new TestReportVo(runId,testId,testVo,resultVo);
				return success(ret);
			}
		}
		return fail();
	}
	
	private TaskRunListQuery convertTaskRunListQueryVo(TaskRunListQueryVo vo) {
		TaskRunListQuery query = new TaskRunListQuery();
		query.setTaskId(vo.getTaskId());
		query.setPageSize(vo.getPageInfo().getPageSize());
		query.setCurPage(vo.getPageInfo().getCurrent());
		return query;
	}

}
