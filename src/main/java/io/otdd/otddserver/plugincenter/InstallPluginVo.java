package io.otdd.otddserver.plugincenter;

import lombok.Data;

@Data
public class InstallPluginVo {
    String existingPluginName;
    String downloadUrl;
}
