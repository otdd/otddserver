package io.otdd.otddserver.vo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.otdd.otddserver.entity.Module;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Type;
import java.util.List;

@Data
public class ModuleVo {
	
	private Integer id;

    private String name;

    private String listenPorts;

    private String protocol;

    private String ignoreLocalPorts;

    private String ignoreRemoteIpPorts;

    private List<PluginSettingsVo> pluginConf;

    private boolean isDelete;
    
	public ModuleVo(Module module){
		this.id = module.getId();
		this.name = module.getName();
		this.protocol = module.getProtocol();
		Gson gson = new Gson();
        Type listType = new TypeToken<List<PluginSettingsVo> > () { }.getType();
        if(!StringUtils.isBlank(module.getPluginConf())) {
			this.pluginConf = gson.fromJson(module.getPluginConf(), listType);
		}
	}

}
