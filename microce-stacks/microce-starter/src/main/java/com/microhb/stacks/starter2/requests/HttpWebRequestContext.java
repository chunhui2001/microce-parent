package com.microce.stacks.starter.requests;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.hashids.Hashids;

/**
 * 用于描述调用链id
 */
public class HttpWebRequestContext {

    private String linkId;  // 调用id, 整个调用链唯一
    private String passType;    // 传递类型, pass OR create
    private String sourceApp; // 该id是哪个应用生成的
    private String currentApp; // 当前应用
    
    private static Hashids hashids = null;
    private static Random random = null;
    
    static {
    	hashids = new Hashids("linkId");
    	random = new Random();
    }

    /**
     * 初始化成功后，将当前对象放入 ThreadLocal, 留待后用
     */
    private static ThreadLocal<HttpWebRequestContext> context = new ThreadLocal<>();

    /**
     * 根据 header 里的 link_id, 生成 HttpWebRequestContext 对象
     * @param request
     */
    public HttpWebRequestContext(HttpServletRequest request, String applicationName) {

        String requestLinkId = RequestAssets.getValueByHeaderKey(request, RequestAssets.REQUEST_LINK_ID);
        String requestLinkIdSourceFrom = RequestAssets.getValueByHeaderKey(request, RequestAssets.REQUEST_LINK_ID_SOURCE_FROM);

        if (StringUtils.isBlank(requestLinkId)) {
            this.setPassType(HttpWebRequestContext.PassTypeEnum.CREATED.toString());
            this.setLinkId(randomLinkId(RequestAssets.REQUEST_LINK_ID));
        } else {
            this.setPassType(HttpWebRequestContext.PassTypeEnum.PASS.toString());
            this.setLinkId(requestLinkId);
        }

        this.setSourceApp(requestLinkIdSourceFrom);

        if (this.isCreatedBySelf()) {
            this.setSourceApp(applicationName);
        }

        this.setCurrentApp(applicationName);

		ThreadContext.put(RequestAssets.REQUEST_LINK_ID, this.getLinkId());

        this.init();
        
    }

    /**
     * 初始化上线文
     */
    private void init() {
        // 将当前对象放入线程上下文
        context.set(this);
    }

    /**
     * 生成随机id
     * @param prefix
     * @return
     */
    private static String randomLinkId(String prefix) {
        return prefix + "_" + hashids.encode(System.currentTimeMillis() + random.nextInt());
    }

    /**
     * 从当前 ThreadLocal 取得请求上下文对象
     * @return
     */
    public static HttpWebRequestContext get() {

        HttpWebRequestContext result = context.get();

        if (result == null) {
            return null;
//            throw new NullPointerException("尚未初始化 HttpWebRequestContext 对象");
        }

        return context.get();
    }

    /**
     * 清空
     */
    public static void free() {
        // 避免 ThreadLocal 内存泄漏
        // context.set(null);
        context.remove();
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getPassType() {
        return passType;
    }

    public void setPassType(String passType) {
        this.passType = passType;
    }

    public String getSourceApp() {
        return sourceApp;
    }

    public void setSourceApp(String sourceApp) {
        this.sourceApp = sourceApp;
    }

    public String getCurrentApp() {
        return currentApp;
    }

    public void setCurrentApp(String currentApp) {
        this.currentApp = currentApp;
    }

    /**
     * 是否是自己创建的
     * @return
     */
    public boolean isCreatedBySelf() {
        return HttpWebRequestContext.PassTypeEnum.CREATED.toString().equals(this.passType);
    }

    /**
     * 是否是传递过来的
     * @return
     */
    public boolean isPassed() {
        return PassTypeEnum.PASS.toString().equals(this.passType);
    }

    /**
     * 传递类型枚举
     */
    public enum PassTypeEnum {
        PASS,   // 传递过来的
        CREATED  // 自己生成的
    }

}
