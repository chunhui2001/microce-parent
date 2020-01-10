package com.microce.stacks.starter.config;

import org.apache.catalina.core.AprLifecycleListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class APRConfig {

    @Value("${tomcat.apr:false}")
    private boolean enabled;
//	@Bean
//	public EmbeddedServletContainerFactory servletContainer() {
//		
//		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
//
//		if (enabled) {
//			AprLifecycleListener arpLifecycle = new AprLifecycleListener();
//			arpLifecycle.setSSLEngine("off");
//			tomcat.setProtocol("org.apache.coyote.http11.Http11AprProtocol");
//			tomcat.addContextLifecycleListeners(arpLifecycle);
//		}
//		
//		return tomcat;
//	}

    @Bean
    public TomcatServletWebServerFactory servletContainer() {

        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

        if (enabled) {
            AprLifecycleListener arpLifecycle = new AprLifecycleListener();
            arpLifecycle.setSSLEngine("off");
            tomcat.setProtocol("org.apache.coyote.http11.Http11AprProtocol");
            tomcat.addContextLifecycleListeners(arpLifecycle);
        }

        return tomcat;
    }

}
