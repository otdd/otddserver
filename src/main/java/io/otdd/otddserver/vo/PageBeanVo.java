package io.otdd.otddserver.vo;

import io.otdd.otddserver.entity.PageBean;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageBeanVo<T> {
    public PageBeanVo(){

    }

    public PageBeanVo(PageBean p){
        this.pageInfo.setCurrent(p.getCurPage());
        this.pageInfo.setPageSize(p.getPageSize());
        this.pageInfo.setTotal(p.getTotalNum());
    }
    private PageInfoVo pageInfo = new PageInfoVo(20);
    List<T> data = new ArrayList<T>();
}
