package io.otdd.otddserver.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PluginSettingsVo {
	
	private String pluginName;

	private String pluginVersion;

	private List<PluginSettingVo> request = new ArrayList<PluginSettingVo>();
	
	private List<PluginSettingVo> response = new ArrayList<PluginSettingVo>();

}
