package io.otdd.otddserver.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageBean<T> {
    int totalNum;
    int pageSize = 20;
    int curPage;
    List<T> data = new ArrayList<T>();
}
