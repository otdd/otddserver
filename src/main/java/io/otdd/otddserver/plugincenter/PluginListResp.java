package io.otdd.otddserver.plugincenter;

import io.otdd.otddserver.entity.PageBean;
import io.otdd.otddserver.entity.PluginInfo;
import io.otdd.otddserver.vo.PageBeanVo;
import io.otdd.otddserver.vo.PageInfoVo;
import lombok.Data;

@Data
public class PluginListResp extends PageBean<PluginInfo> {
}
