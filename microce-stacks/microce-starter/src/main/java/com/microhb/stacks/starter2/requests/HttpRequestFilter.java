package com.microce.stacks.starter.requests;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.microce.plugin.json.JsonUtils;

//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpRequestFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(HttpRequestFilter.class);

	@Value("${spring.application.name}")
	private String applicationName;

	@SuppressWarnings("unused")
	private FilterConfig filterConfig;

	HttpWebRequestContext httpWebRequestContext;

	@Override
	public void init(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	/**
	 * 初始化请求链对象
	 * 
	 * @param request
	 * @return
	 */
	private HttpServletRequest init(ServletRequest request) {
		HttpServletRequest req = (HttpServletRequest) request;
		httpWebRequestContext = new HttpWebRequestContext(req, applicationName);
		return req;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = init(request);
		HttpServletResponse res = (HttpServletResponse) response;
		RequestWrapper requestWrapper = new RequestWrapper(req);

		if (RequestAssets.isStaticResource(req.getRequestURI())) {
			chain.doFilter(requestWrapper, response);
			return;
		}

		try {

			String requestLinkId = httpWebRequestContext.getLinkId();

			// 拿到 REQUEST_LINK_ID 后, 将其放入线程上下文中
			ThreadContext.put(RequestAssets.REQUEST_LINK_ID, requestLinkId);
			// 修改 header, 加入自定义 header
			putCustomerHeaders(requestWrapper);

			Long start = printLogBefore(requestWrapper, requestLinkId);
			chain.doFilter(requestWrapper, response);
			printLogAfter(req, res, requestLinkId, start);

		} finally {
			// 释放 ThreadLocal, 避免内存泄漏
			HttpWebRequestContext.free();
		}

	}

	/**
	 * 处理前打印日志
	 * 
	 * @param req
	 * @param requestLinkId
	 * @return
	 * @throws IOException
	 */
	public Long printLogBefore(RequestWrapper req, String requestLinkId) throws IOException {

		String getRemoteAddress = RequestAssets.getRemoteAddr(req);
		String headerString = RequestAssets.getHeadersString(req);
		String uri = RequestAssets.getFullRequestUri(req);

		Long start = System.currentTimeMillis();

		if (!RequestAssets.skipPrintLog(req)) {
			logger.info("[请求开始#{}][{} {}][REQUEST_HEADERS {}][REMOTE_ADDR {}][REQUEST_BODY {}]", requestLinkId,
					req.getMethod(), uri, headerString, getRemoteAddress, req.getRequestBody());
		}

		return start;

	}
	

	/**
	 * 处理后打印日志
	 * 
	 * @param req
	 * @param requestLinkId
	 * @param start
	 * @throws IOException
	 */
	public void printLogAfter(HttpServletRequest req, HttpServletResponse res, String requestLinkId, Long start) {
		RequestAssets.cors(res);
		if (!RequestAssets.skipPrintLog(req)) {
			logger.info("[请求结束#{}][{} {}][响应状态码:{}][耗时:{}]", requestLinkId, req.getMethod(),
					RequestAssets.getFullRequestUri(req), res.getStatus(), (System.currentTimeMillis() - start) + "毫秒");
		}
	}

	public static void printLogForController(Map<String, Object> params) {

		// 接收到请求，记录请求内容
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		if (!"POST".equals(request.getMethod())) {
			return;
		}

		HttpWebRequestContext ctx = HttpWebRequestContext.get();
		String requestLinkId = null;

		if (ctx != null)
			requestLinkId = ctx.getLinkId();

		String paramString = JsonUtils.toJsonString(params);

		if (paramString == null) {
			paramString = " 请求参数包含无法序列化对象 ";
		}

		logger.info("[请求数据包#{}][{}] >>{}<<", requestLinkId, RequestAssets.getFullRequestUri(request), paramString);

	}

	/**
	 * 将一些自定义header放入请求头
	 * 
	 * @param requestWrapper
	 */
	public void putCustomerHeaders(RequestWrapper requestWrapper) {

		requestWrapper.addHeader(RequestAssets.REQUEST_LINK_ID, httpWebRequestContext.getLinkId());
		requestWrapper.addHeader(RequestAssets.REQUEST_LINK_ID_SOURCE_FROM, httpWebRequestContext.getSourceApp());
		requestWrapper.addHeader(RequestAssets.REQUEST_LINK_ID_PASS_TYPE, httpWebRequestContext.getPassType());
		requestWrapper.addHeader(RequestAssets.REQUEST_CURRENT_APPLICATION_NAME, applicationName);

		if (!RequestAssets.hasHeadersKey((HttpServletRequest) requestWrapper,
				RequestAssets.REQUEST_FROM_APPLICATION_NAME.toString())) {
			requestWrapper.addHeader(RequestAssets.REQUEST_FROM_APPLICATION_NAME, "N/A");
		}
	}

	/**
	 * 向 feign client 加入自定义header
	 * 
	 * @param template
	 * @param applicationName
	 */
	public static void putCustomerHeaders(feign.RequestTemplate template, String applicationName) {
		// HttpWebRequestContext httpWebRequestContext = new
		// HttpWebRequestContext(RequestAssets.getHttpServletRequest(),
		// applicationName);
		HttpWebRequestContext httpWebRequestContext = HttpWebRequestContext.get();
		if (httpWebRequestContext == null)
			return;
		template.header(RequestAssets.REQUEST_LINK_ID, httpWebRequestContext.getLinkId());
		template.header(RequestAssets.REQUEST_LINK_ID_SOURCE_FROM,
				httpWebRequestContext.getSourceApp() == null ? "N/A" : httpWebRequestContext.getSourceApp());
		template.header(RequestAssets.REQUEST_LINK_ID_PASS_TYPE, httpWebRequestContext.getPassType());
		template.header(RequestAssets.REQUEST_FROM_APPLICATION_NAME, applicationName);
	}

	@Override
	public void destroy() {

	}

}
