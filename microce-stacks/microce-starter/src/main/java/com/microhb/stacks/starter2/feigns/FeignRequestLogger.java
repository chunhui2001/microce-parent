package com.microce.stacks.starter.feigns;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.microce.plugin.commons.Base64Utils;
import com.microce.plugin.commons.RegexUtils;
import com.microce.plugin.json.JsonUtils;
import com.microce.plugin.response.R;

import feign.Request;
import feign.Response;

/**
 * Feign client 调用日志
 * 
 * @author keesh
 */
@Component
public class FeignRequestLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(FeignRequestLogger.class);

	public Object completed(Response response, Request feignRequest, R<Object> result, Type type) {

		String requestUrl = Base64Utils.urlDecode(feignRequest.url());
		String requestParam = feignRequest.requestBody() == null ? "N/A" : prettyRequestBody(feignRequest.requestBody().asString());

		String responseContent = null;
		Object o = null;

		if (result.getCode().equals(R.FUN)) {
			responseContent = result.getMessage();
			o = JsonUtils.parseJsonString(result.getMessage(), type.getClass());
		} else {
			responseContent = JsonUtils.toJsonString(result);
			o = result;
		}

		LOGGER.info("FEIGN远程调用已完成<COMPLETED>:[调用地址] >>{}<< [请求参数] >>{}<< [返回结果] >>{}<<", requestUrl, requestParam, responseContent);

		return o;
	}

	public R<Object> error(Response response, Request feignRequest, String bodyString, R<?> restResponse) {

		String requestUrl = Base64Utils.urlDecode(feignRequest.url());
		String requestParam = feignRequest.requestBody() == null ? "N/A" : prettyRequestBody(feignRequest.requestBody().asString());

		Integer code = null;
		String message = null;
		String errorShowMessage = null;

		if (restResponse != null) {
			code = restResponse.getCode();
			message = restResponse.getMessage();
			errorShowMessage = JsonUtils.toJsonString(restResponse);
		} else {
			code = response.status();
			message = bodyString;
			errorShowMessage = message;
		}

		LOGGER.error("FEIGN远程调用已完成<ERROR>:[调用地址] >>{}<< [状态码] >>{}<< [请求参数] >>{}<< [返回结果] >>{}<<", requestUrl, code, requestParam, errorShowMessage);


		R<Object> result = new R<>();
		result.setCode(code);
		result.setMessage(message);

		return result;

	}

	public R<Object> failed(Response response, Request feignRequest, String bodyString, R<?> restResponse) {

		String requestUrl = Base64Utils.urlDecode(feignRequest.url());
		String requestParam = feignRequest.requestBody() == null ? "N/a" : prettyRequestBody(feignRequest.requestBody().asString());

		Integer code = null;
		String message = null;
		String errorShowMessage = null;

		if (restResponse != null) {
			code = restResponse.getCode();
			message = restResponse.getMessage();
			errorShowMessage = JsonUtils.toJsonString(restResponse);
		} else {
			code = response.status();
			message = bodyString;
			errorShowMessage = message;
		}

		LOGGER.error("FEIGN远程调用已完成<FAILED>:[调用地址] >>{}<< [状态码] >>{}<< [请求参数] >>{}<< [返回结果] >>{}<<", requestUrl, code, requestParam, errorShowMessage);

		R<Object> result = new R<>();
		result.setCode(code);
		result.setMessage(message);

		return result;

	}

	public static String prettyRequestBody(String body) {

		String binaryBody = RegexUtils.isBinaryBody(body);

		if (binaryBody != null) {
			return binaryBody;
		}

		return body;
	}

}
