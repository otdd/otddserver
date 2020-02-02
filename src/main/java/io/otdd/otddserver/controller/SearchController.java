package io.otdd.otddserver.controller;

import com.google.gson.Gson;
import io.otdd.otddserver.search.TestStoreType;
import io.otdd.otddserver.search.SearchQuery;
import io.otdd.otddserver.search.SearchResult;
import io.otdd.otddserver.service.EditService;
import io.otdd.otddserver.service.TestService;
import io.otdd.otddserver.vo.SearchQueryVo;
import io.otdd.otddserver.vo.SearchResultVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

@Controller
@EnableAutoConfiguration
@RequestMapping("/otdd/search")
public class SearchController extends BaseController{

	@Autowired
    private TestService testService;

	@Autowired
	private EditService editService;

	@RequestMapping(value = "/search")
	@ResponseBody
	public Map<String, Object> search(@RequestBody String body) {
		Gson gson = new Gson();
		SearchQueryVo searchQueryVo = gson.fromJson(body, SearchQueryVo.class);
		SearchQuery searchQuery = changeSearchQuery(searchQueryVo);
		SearchResult searchResult = null;
		if(TestStoreType.EDITED_TEST.equalsIgnoreCase(searchQuery.getTestStoreType())){
			searchResult = editService.search(searchQuery);
		}
		else{
			searchResult = testService.search(searchQuery);
		}
		if(searchResult!=null){
			SearchResultVo searchResultVo = new SearchResultVo(searchResult);
			return success(searchResultVo);
		}
		return fail();
	}
	
	private SearchQuery changeSearchQuery(SearchQueryVo vo) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SearchQuery ret = new SearchQuery();
		ret.setInboundReq(vo.getInboundReq());
		ret.setInboundResp(vo.getInboundResp());
		ret.setCurPage(vo.getPageInfo().getCurrent());
		if(!StringUtils.isEmpty(vo.getEndTime())){
			try{
				ret.setEndTime(df.parse(vo.getEndTime()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ret.setPageSize(vo.getPageInfo().getPageSize());
		if(!StringUtils.isEmpty(vo.getStartTime())){
			try{
				ret.setStartTime(df.parse(vo.getStartTime()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ret.setModuleId(vo.getModuleId());
		ret.setTestId(vo.getTestId());
		ret.setOutboundReq(vo.getOutboundReq());
		ret.setOutboundResp(vo.getOutboundResp());
		if(TestStoreType.ONLINE_RECORDED_TEST.equalsIgnoreCase(vo.getTestStoreType())){
			ret.setTestStoreType(TestStoreType.ONLINE_RECORDED_TEST);
		}
		else if(TestStoreType.EDITED_TEST.equalsIgnoreCase(vo.getTestStoreType())){
			ret.setTestStoreType(TestStoreType.EDITED_TEST);
		}
		return ret;
	}

}
