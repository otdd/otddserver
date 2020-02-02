package io.otdd.otddserver.entity;

import lombok.Data;

@Data
public class PluginInfo {

    public static int OPERATION_ADD = 0;
    public static int OPERATION_REPLACE = 1;
    public static int OPERATION_DO_NOTHING = 2;

    String pluginName;
    String protocol;
    String description;
    String pluginVersion;
    String downloadUrl;
    String installedPluginName;
    String installedVersion;
    int operation;
}
