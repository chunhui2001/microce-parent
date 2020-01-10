package com.microce.stacks.starter.config;


import org.apache.log4j.LogManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;


/**
 * 此类用于解决 log4j 可能的内存泄漏
 * @author keesh
 */
@Component
public class ImplDisposableBean implements DisposableBean, ExitCodeGenerator {

    @Override
    public void destroy() throws Exception {
        LogManager.shutdown();
    }

    @Override
    public int getExitCode() {
        return 0;
    }
}