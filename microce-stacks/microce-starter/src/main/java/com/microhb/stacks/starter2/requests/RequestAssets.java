package com.microce.stacks.starter.requests;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.microce.plugin.commons.Base64Utils;
import com.microce.plugin.commons.StringComparator;
import com.microce.plugin.json.JsonUtils;

/**
 * HTTP REQUEST Helper
 * 
 * @author keesh
 */
public class RequestAssets {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestAssets.class.getName());

    // public static final String REQUEST_UNIQUE_ID = "_request_unique_id"; //
    // 每次调用唯一,

    public static final String REQUEST_CURRENT_APPLICATION_NAME = "_ra_app"; // 用于标明接受当前请求的服务
    public static final String REQUEST_FROM_APPLICATION_NAME = "_rb_from"; // 用于标明是来自哪个服务的请求
    public static final String REQUEST_LINK_ID = "_rc_lid"; // 整个调用链唯一
    public static final String REQUEST_LINK_ID_PASS_TYPE = "_rd_type"; // 用于说明当前调用链id是传递过来的，还是自己生成的
    public static final String REQUEST_LINK_ID_SOURCE_FROM = "_re_src"; // 用于说明当前调用链id是传递过来的，是哪个应用生成的

    /**
     * 取得请求头
     * 
     * @param request
     * @return
     */
    public static Map<String, String> getHeadersMap(HttpServletRequest request) {

        Map<String, String> map = new HashMap<String, String>();

        @SuppressWarnings("rawtypes")
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    /**
     * 判断url是否是静态资源文件
     * 
     * @param uri
     * @return
     */
    public static boolean isStaticResource(String uri) {

        int lastCharIndex = uri.toLowerCase().lastIndexOf(".");

        if (lastCharIndex == -1)
            return false;

        String s = uri.substring(lastCharIndex).toLowerCase();

        if (s.equals(".json"))
            return false;

        Vector<String> v = new Vector<String>(5);

        v.add(".ico");
        v.add(".css");
        v.add(".js");
        v.add(".jpg");
        v.add(".jpeg");

        return v.contains(s);

    }

    /**
     * 取得客户端ip地址
     * 
     * @param request
     * @return
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        String ipFromHeader = request.getHeader("X-FORWARDED-FOR");
        if (ipFromHeader != null && ipFromHeader.length() > 0) {
            return ipFromHeader;
        }
        return request.getRemoteAddr();
    }

    /**
     * 取得当前请求实例
     * 
     * @return
     */
    public static HttpServletRequest getHttpServletRequest() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        return request;
    }

    /**
     * 根据 header 里的 key 取得 value
     */
    public static String getValueByHeaderKey(HttpServletRequest request, String headerKey) {
        Map<String, String> requestMap = RequestAssets.getHeadersMap(request);
        if (requestMap != null && requestMap.containsKey(headerKey)) {
            return requestMap.get(headerKey);
        }
        return null;
    }

    /**
     * header 里面是否包含指定key
     * 
     * @param request
     * @param headerKey
     * @return
     */
    public static boolean hasHeadersKey(HttpServletRequest request, String headerKey) {
        return getValueByHeaderKey(request, headerKey) != null;
    }

    public static String getHttpBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath() + "/";
    }

    public static String getQueryString(String getQueryString) {
        return getQueryString != null && getQueryString.trim().length() > 0 ? "?" + getQueryString
                : "";
    }

    public static String getHeadersString(HttpServletRequest request) {

        Map<String, String> map = RequestAssets.getHeadersMap(request);

        if (map == null || map.size() == 0)
            return "N/A";

        // map.put("short-agent", getShortAgent(map.get("user-agent")));

        // 根据key排序, 方便日志查看
        Map<String, String> sortMap = new TreeMap<>(new StringComparator());

        sortMap.putAll(map);

        sortMap.remove("accept");
        sortMap.remove("accept-language");
        sortMap.remove("accept-encoding");
        sortMap.remove("user-agent");
        sortMap.remove("cache-control");
        sortMap.remove("connection");
        sortMap.remove("upgrade-insecure-requests");
        sortMap.remove("host");
        sortMap.remove("_rc_lid");

        return "" + JsonUtils.toJsonString(sortMap) + "";

    }

    public static String getShortAgent(String agent) {
        return agent;
    }

    /**
     * 返回全路径
     * 
     * @param request
     * @return
     */
    public static String getFullRequestUri(HttpServletRequest request) {
        String result = RequestAssets.getHttpBaseUrl(request) + request.getRequestURI().substring(1)
                + RequestAssets.getQueryString(request.getQueryString());
        return Base64Utils.urlDecode(result);
    }

    public static boolean skipPrintLog(HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/manage")) {
            return true;
        }
        return false;
    }



    /**
     * 是否是 ajax 请求
     * 
     * @return
     */
    public static boolean isAjax(HttpServletRequest request) {

        String ajaxHeader = request.getHeader("X-Requested-With");
        String userAgent = request.getHeader("user-agent").toLowerCase();
        boolean isAjax = "XMLHttpRequest".equals(ajaxHeader);

        if (!isAjax) {
            isAjax = userAgent.startsWith("curl/") || userAgent.startsWith("Postman") //
                    || userAgent.startsWith("postman") //
                    || userAgent.startsWith("Java/")//
                    || userAgent.startsWith("java/");
            if (!isAjax) {
                // 是否是代理转发
                String forwaredProto = request.getHeader("x-forwarded-proto");
                String forwaredPort = request.getHeader("x-forwarded-port");
                String forwaredHost = request.getHeader("x-forwarded-host");
                String forwaredFor = request.getHeader("x-forwarded-for");

                if (!StringUtils.isAnyBlank(forwaredProto, forwaredPort, forwaredHost,
                        forwaredFor)) {
                    isAjax = true;
                    LOGGER.info(
                            "本次请求是代理转发 ajax, method={}, url={}, forwaredProto={}, forwaredPort={}, forwaredHost={}, forwaredFor={}",
                            request.getMethod(), request.getRequestURI(), forwaredProto,
                            forwaredPort, forwaredHost, forwaredFor);
                }

                if (!isAjax) {
                    // refer
                    String referer = request.getHeader("referer");
                    String upgrade = request.getHeader("upgrade-insecure-requests");
                    if (!StringUtils.isBlank(referer) && StringUtils.isBlank(upgrade)) {
                        isAjax = true;
                        LOGGER.info(
                                "本次请求可能是 ajax, method={}, url={}, refer={}, upgradeInsecureRequests=N/a", //
                                request.getMethod(), request.getRequestURI(), referer, upgrade);
                    }
                }

            }
        }

        return isAjax;
    }

    /**
     * 根据自定义 header 取得 value
     */
    public static Boolean hasHeader(HttpServletRequest request, String headerKey) {
        if (StringUtils.isBlank(headerKey))
            return false;

        @SuppressWarnings("rawtypes")
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            if (((String) headerNames.nextElement()).equals(headerKey))
                return true;
        }

        return false;
    }

    /**
     * 根据自定义 header 取得 value
     */
    public static String getHeader(HttpServletRequest request, String headerKey) {
        if (StringUtils.isBlank(headerKey))
            return null;
        return request.getHeader(headerKey);
    }

    /**
     * 设置允许跨域
     * 
     * @param response
     */
    public static HttpServletResponse cors(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");
        return response;
    }

}
