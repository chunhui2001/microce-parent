package com.microce.stacks.starter.config;

import org.apache.catalina.filters.RemoteIpFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import com.microce.stacks.starter.requests.HttpRequestFilter;

import feign.Feign;
import feign.RequestInterceptor;

//@EnableWebMvc
@Configuration
public class AppConfig implements WebMvcConfigurer, ApplicationListener<ApplicationEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

	@Autowired
	private Environment environment;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${server.tomcat.basedir}")
	private String tomcatBasedir;

	@Value("${server.port}")
	private String serverPort;

	@Value("${info.pubtime:0}")
	private String publishTime;

	/**
	 * 设置密码加密方式
	 * 
	 * @return
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 设置默认序列化方式
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.defaultContentType(MediaType.APPLICATION_JSON_UTF8);
		// 根据扩展名自动设置 mimeType
		configurer.favorPathExtension(true);
	}

	/**
	 * 增加静态资源路径, spring.resources.add-mappings=false
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 设置了throw-exception-if-no-handler-found: true，就会覆盖默认的 static 访问路径，
		// 自定义加了这个路径又会覆盖这个 throw-exception-if-no-handler-found: true；
		// 解决办法就是设置自定义 static 路径的时候，不要使用 /**，而是自己给加一个前缀 /static/**
		registry.addResourceHandler("/assets/**") //
				.addResourceLocations("classpath:/public/assets/") //
				.setCachePeriod(3600).resourceChain(true) //
				.addResolver(new PathResourceResolver());

		registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/public/");
	}

	/**
	 * 设置拦截器
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
	}

	/**
	 * 设置跨域资源访问
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")// 设置允许跨域的路径
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")// 设置允许的方法
				.allowedOrigins("*")// 设置允许跨域请求的域名
				.allowedHeaders("*").maxAge(3600); // 跨域允许时间
	}

	@Bean
	public RemoteIpFilter remoteIpFilter() {
		return new RemoteIpFilter();
	}

	/**
	 * 设置 feign client
	 * 
	 * @return
	 */
	@Bean
	public Feign.Builder feignBuilder() {
		return Feign.builder().requestInterceptor(new RequestInterceptor() {
			public void apply(feign.RequestTemplate template) {
				// template.header(RequestAssets.REQUEST_UNIQUE_ID,
				// UUID.randomUUID().toString());
				HttpRequestFilter.putCustomerHeaders(template, applicationName);
			}
		});
	}

	public void onApplicationEvent(ApplicationEvent event) {

		if (event instanceof ApplicationReadyEvent) {
			LOGGER.info("Spring Boot. ({}) {} 已启动, tomcatBasedir={}, 端口={} ", getVersion(),
					ThreadContext.get("appname"), this.getTomcatHome(), getTomcatPort());
			return;
		}
	}

	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
		return new TomcatCustomizer(getTomcatHome());
	}

	/**
	 * 获取主目录
	 * 
	 * @return
	 */
	public String getTomcatHome() {
		return StringUtils.isBlank(this.tomcatBasedir) ? "N/a" : this.tomcatBasedir;
	}

	public String getTomcatPort() {
		return environment.getProperty("local.server.port");
	}

	/**
	 * 获取 spring 版本
	 * 
	 * @return
	 */
	public static String getVersion() {
		Package pkg = SpringBootVersion.class.getPackage();
		String springVersion = (pkg != null ? pkg.getImplementationVersion() : null);
		return springVersion;
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		// DO NOT SET configurer.enable()
		// configurer.enable();
	}

}
