package io.otdd.otddserver.plugincenter;

import io.otdd.otddserver.vo.PageInfoVo;
import lombok.Data;

@Data
public class PluginListReq {
    private String otddVersion;
    private PageInfoVo pageInfo = new PageInfoVo(10);
}
