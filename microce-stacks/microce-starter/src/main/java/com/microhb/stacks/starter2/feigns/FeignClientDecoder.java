package com.microce.stacks.starter.feigns;

import java.io.IOException;
import java.lang.reflect.Type;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.microce.plugin.json.JsonUtils;
import com.microce.plugin.response.R;
import com.microce.plugin.response.RestException;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;

@Component
@SuppressWarnings("unchecked")
public class FeignClientDecoder extends JacksonDecoder implements ErrorDecoder {

    private ErrorDecoder delegate = new ErrorDecoder.Default();

    @Autowired
    private FeignRequestLogger feignRequestLogger;

	@Override
    public Exception decode(String methodKey, Response response) {

        HttpStatus statusCode = HttpStatus.valueOf(response.status());

        byte[] responseBody = null;
        String bodyString = null;
        R<Object> restResponse = new R<>();
        //String applicationName = response.reason();     // 当发生异常时将服务名存到 reason

        try {
            responseBody = IOUtils.toByteArray(response.body().asInputStream());
            bodyString = new String(responseBody);
            restResponse = JsonUtils.parseJsonString(bodyString, restResponse.getClass());
        } catch (IOException e) {
            throw new RuntimeException("Failed to process response body.", e);
        }

        if (statusCode.value() >= 400 && statusCode.value() <= 499) {
            restResponse = feignRequestLogger.failed(response, response.request(), bodyString, restResponse);
            if (restResponse.getCode() == null) throw new RestException(statusCode.value(), restResponse.getMessage());
            throw new RestException(restResponse.getCode(), restResponse.getMessage());
        }

        if (statusCode.value() >= 500 && statusCode.value() <= 599) {
            restResponse = feignRequestLogger.error(response, response.request(), bodyString, restResponse);
            if (restResponse.getCode() == null) throw new RestException(statusCode.value(), restResponse.getMessage());
            throw new RestException(restResponse.getCode(), restResponse.getMessage());
        }

        return delegate.decode(methodKey, response);
    }

    /**
     * 拦截 feign 调用返回的结果
     * @param response
     * @param type
     * @return
     * @throws IOException
     * @throws DecodeException
     * @throws FeignException
     */
    @Override
    public Object decode(Response response, Type type) {

        Object o = null;
        try {
            o = super.decode(response, type);
        } catch (IOException e) {
            throw new RestException(R.SERVER_ERROR, e.getMessage());
        }

        R<Object> result = (R<Object>) o;

//        if (result.getResult() == null) {
//         return result;
//        }

        if (!R.isok(result.getCode())) {
            result = feignRequestLogger.failed(response, response.request(), null, result);
//            throw new RestException(result.getCode(), JsonUtils.toJsonString(result));
            throw new RestException(result.getCode(), result.getMessage());
        }

        o = feignRequestLogger.completed(response, response.request(), result, type);

        return o;

    }

}
