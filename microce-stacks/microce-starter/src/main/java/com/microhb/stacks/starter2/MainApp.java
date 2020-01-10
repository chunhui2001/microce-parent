package com.microce.stacks.starter;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.FileSystemUtils;

import com.microce.stacks.starter.config.AppConfig;

//@EnableDiscoveryClient
//@EnableAutoConfiguration()
//@ComponentScan(basePackages = {"com.microce"})
@SpringBootApplication
public class MainApp implements CommandLineRunner {

    @Autowired
    AppConfig appConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) throws Exception {
    	SpringApplication.run(MainApp.class, args);
//        startup(MainApp.class, args);
    }

    public static ConfigurableApplicationContext startup(Class<?> clazz, String... args) {
        // debugger 时拿不到 implementation 相关信息
        ThreadContext.put("appname", clazz.getPackage().getImplementationTitle());
        BasicConfigurator.configure();
        SpringApplication app = new SpringApplication(clazz);
        return app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("当前启动参数: args={}", args.length == 0 ? "(N/a)" : String.join(",", args));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                FileSystemUtils.deleteRecursively(new File(appConfig.getTomcatHome()));
                LOGGER.info("当前服务已停止!");
            }
        });
    }

}
