package com.microce.stacks.starter.config;

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.util.FileSystemUtils;

public class TomcatCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private String home;

    public TomcatCustomizer(String home) {
        this.home = home;
    }

    void customizeTomcat(TomcatServletWebServerFactory factory) {

        factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
            connector.setAttribute("relaxedPathChars", "<>[\\]^`{|}");
            connector.setAttribute("relaxedQueryChars", "<>[\\]^`{|}");
        });

        factory.addContextCustomizers((context) -> {
            String docBase = context.getDocBase();
            FileSystemUtils.deleteRecursively(new File(docBase));
            String tempPath = Paths.get(home, "doc_base").toString();
            File file = new File(tempPath);
            file.mkdirs();
            context.setDocBase(file.getAbsolutePath());
        });

    }

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        if (factory instanceof TomcatServletWebServerFactory) {
            if (!StringUtils.isBlank(home)) {
                customizeTomcat((TomcatServletWebServerFactory) factory);
            }
        }
    }

}
