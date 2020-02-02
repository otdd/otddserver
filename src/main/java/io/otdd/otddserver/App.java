package io.otdd.otddserver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.otdd.ddl.plugin.DDLCodecFactory;
import io.otdd.otddserver.grpc.OtddServerServiceImpl;
import io.otdd.otddserver.plugin.PluginMgr;
import io.otdd.otddserver.grpc.TestRunnerServiceImpl;
import io.otdd.otddserver.service.SysConfService;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ComponentScan("io.otdd.otddserver")
public class App implements CommandLineRunner{

	@Autowired
	private ApplicationContext appContext;

	public static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	@Override
	public void run(String... args) throws Exception {

		int testRunnerGrpcPort = 8764;
		Server server = ServerBuilder.forPort(testRunnerGrpcPort)
				.addService(appContext.getBean(TestRunnerServiceImpl.class))
				.addService(appContext.getBean(OtddServerServiceImpl.class))
				.build();
		try {
			server.start();
		}
		catch(Exception e){
			e.printStackTrace();
			LOGGER.error("failed to start otdd grpc server! exception:"+e.getMessage());
			System.exit(1);
		}

		// Server threads are running in the background.
		LOGGER.info("otdd grpc server started on port:"+testRunnerGrpcPort);

		//init plugins.
		LOGGER.info("start to init plugins..");
		SysConfService sysConfigService = appContext.getBean(SysConfService.class);
		String pvRootPath = appContext.getEnvironment().getProperty("pv.rootpath");
		String pluginPath = pvRootPath+(pvRootPath.endsWith("/")?"":"/")+"plugins";
		PluginMgr.getInstance().init(pluginPath,sysConfigService.getConfigByKey("plugin_conf"));
		Map<String,DDLCodecFactory> factories = PluginMgr.getInstance().getDDLCodecFactories();
		StringBuilder sb = new StringBuilder();
		for(String pluginName:factories.keySet()){
			sb.append(pluginName+",");
		}
		LOGGER.info("finish to init plugins..  pluginName list:["+
				(sb.length()==0?"no pluginName loaded.":sb.substring(0,sb.length()-1))+"]");

		long interval = TimeUnit.SECONDS.toMillis(1);
		IOFileFilter directories = FileFilterUtils.and(
				FileFilterUtils.directoryFileFilter(),
				HiddenFileFilter.VISIBLE);
		IOFileFilter files    = FileFilterUtils.and(
				FileFilterUtils.fileFileFilter(),
				FileFilterUtils.suffixFileFilter(".jar"));

		IOFileFilter filter = FileFilterUtils.or(directories, files);

		FileAlterationObserver observer = new FileAlterationObserver(new File(pluginPath), filter);

		observer.addListener(new FileListener(pluginPath,sysConfigService));
		FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
		try{
			monitor.start();
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

	public static void main(String args[]){
		LOGGER.info("starting otdd server..");
		SpringApplication.run(App.class, args);
		LOGGER.info("otdd server started.");
	}

}

class FileListener extends FileAlterationListenerAdaptor {

	public static final Logger LOGGER = LoggerFactory.getLogger(FileListener.class);

	private String pluginPath;
	private SysConfService sysConfService;
	public FileListener(String pluginPath,SysConfService sysConfService){
		this.pluginPath = pluginPath;
		this.sysConfService = sysConfService;
	}

	private void reInit(){
		PluginMgr.getInstance().reInit(this.pluginPath,this.sysConfService.getConfigByKey("plugin_conf"));
		Map<String,DDLCodecFactory> factories = PluginMgr.getInstance().getDDLCodecFactories();
		StringBuilder sb = new StringBuilder();
		for(String pluginName:factories.keySet()){
			sb.append(pluginName+",");
		}
		LOGGER.info("finish to re-init plugins..  pluginName list:["+
				(sb.length()==0?"no pluginName loaded.":sb.substring(0,sb.length()-1))+"]");
	}

	public void onFileCreate(File file) {
		this.reInit();
	}
	public void onFileChange(File file) {
		this.reInit();
	}
	public void onFileDelete(File file) {
		this.reInit();
	}
	public void onDirectoryCreate(File directory) {
	}
	public void onDirectoryChange(File directory) {
	}
	public void onDirectoryDelete(File directory) {
	}
}