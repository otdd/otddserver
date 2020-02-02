package io.otdd.otddserver.service;

import io.otdd.ddl.plugin.DDLCodecFactory;
import io.otdd.otddserver.entity.PageBean;
import io.otdd.otddserver.entity.PluginInfo;
import io.otdd.otddserver.plugin.PluginMgr;
import io.otdd.otddserver.plugincenter.PluginListReq;
import io.otdd.otddserver.plugincenter.PluginListResp;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

@Service
@EnableAutoConfiguration
public class PluginService {

    @Value("${plugincenter.host}")
    protected String pluginCenterHost;

    @Value("${otddserver.version}")
    protected String version;

    @Value("${pv.rootpath}")
    protected String pvRootPath;

    @Autowired
    protected SysConfService sysConfigService;

    public PageBean<PluginInfo> getPluginListInStore(int curPage, int pageSize) {
        PageBean<PluginInfo> ret = new PageBean<PluginInfo>();
        ret.setPageSize(pageSize);
        ret.setCurPage(curPage);

        PluginListReq req = new PluginListReq();
        req.setOtddVersion(version);
        req.getPageInfo().setPageSize(pageSize);
        req.getPageInfo().setCurrent(curPage);
        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            RestTemplate restTemplate = new RestTemplate(requestFactory);
            PluginListResp resp = restTemplate.postForObject(pluginCenterHost + (pluginCenterHost.endsWith("/")?"":"/")+"plugincenter/plugin/getPluginList", req, PluginListResp.class);
            for(PluginInfo pluginInfo:resp.getData()){
                pluginInfo.setOperation(PluginInfo.OPERATION_ADD);
                DDLCodecFactory factory = PluginMgr.getInstance().getDDLCodecFactoryByProtocol(pluginInfo.getProtocol());
                if(factory!=null){
                    pluginInfo.setOperation(PluginInfo.OPERATION_REPLACE);
                    if(factory.getPluginName().equalsIgnoreCase(pluginInfo.getPluginName())
                            &&factory.getPluginVersion().equalsIgnoreCase(pluginInfo.getPluginVersion())){
                        pluginInfo.setOperation(PluginInfo.OPERATION_DO_NOTHING);
                    }
                    pluginInfo.setInstalledVersion(factory.getPluginVersion());
                    pluginInfo.setInstalledPluginName(factory.getPluginName());
                }
            }
            ret.setData(resp.getData());
            ret.setPageSize(resp.getPageSize());
            ret.setCurPage(resp.getCurPage());
            ret.setTotalNum(resp.getTotalNum());
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean installPlugin(String existingPluginName, String downloadUrl) {
        String pluginPath = pvRootPath+(pvRootPath.endsWith("/")?"":"/")+"plugins/";
        File tmp = new File(pluginPath);
        if(!tmp.exists()){
            tmp.mkdirs();
        }
        String tmpPluginPath = pvRootPath+(pvRootPath.endsWith("/")?"":"/")+"plugins-tmp/";
        tmp = new File(tmpPluginPath);
        if(!tmp.exists()){
            tmp.mkdirs();
        }

        //download the plugin into tmp folder
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")+1);
        String tmpfileName = tmpPluginPath+fileName;
        try {
            FileUtils.copyURLToFile(
                    new URL(downloadUrl),
                    new File(tmpfileName),
                    10000,
                    60000);
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }

        //remove existing plugin
        if(StringUtils.isNotBlank(existingPluginName)) {
            try {
                DDLCodecFactory factory = PluginMgr.getInstance().getDDLCodecFactory(existingPluginName);
                if (factory != null) {
                    String classFullName = factory.getClass().getName();
                    String className = classFullName.substring(classFullName.lastIndexOf('.') + 1) + ".class";
                    URL fullPath = factory.getClass().getResource(className);
                    if (fullPath != null) {
                        String path = fullPath.getPath();
                        String filePath = path.substring(path.indexOf(':') + 1, path.indexOf('!'));
                        (new File(filePath)).delete();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        //move from tmp to plugin folder
        try {
            FileUtils.moveFile(new File(tmpfileName), new File(pluginPath + fileName));
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return true;

    }

    public boolean deletePlugin(String pluginName) {
        try {
            DDLCodecFactory factory = PluginMgr.getInstance().getDDLCodecFactory(pluginName);
            if (factory != null) {
                String classFullName = factory.getClass().getName();
                String className = classFullName.substring(classFullName.lastIndexOf('.') + 1) + ".class";
                URL fullPath = factory.getClass().getResource(className);
                if (fullPath != null) {
                    String path = fullPath.getPath();
                    String filePath = path.substring(path.indexOf(':') + 1, path.indexOf('!'));
                    (new File(filePath)).delete();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
