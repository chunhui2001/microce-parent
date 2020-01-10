package com.microce.stacks.starter.exception;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microce.plugin.response.R;
import com.microce.plugin.response.RestException;

@EnableWebMvc	// 加此注解才能重写系统默认的 404 和 500
@ControllerAdvice
public class GlobalExceptionHandler {

	 private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@Value("${spring.application.name}")
	private String applicationName;
    
	/**
	 * 404异常
	 * @return
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(value= HttpStatus.OK)
    @ResponseBody
    public R<?> requestHandlingNoHandlerFound(HttpServletRequest request, HttpServletResponse response, NoHandlerFoundException e) {
		return R.failed(R.NOT_FOUND, "[" + applicationName + "]<" + request.getRequestURI() + ">");
    }

    /**
     * 400异常
     * @return
     * @throws IOException 
     * @throws JsonProcessingException 
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(value= HttpStatus.OK)
    @ResponseBody
    public R<?> HttpMediaTypeNotAcceptableExceptionHandler(HttpServletRequest request, HttpServletResponse response, HttpMediaTypeNotAcceptableException e) {
        return R.failed(R.FAILED, "[" + applicationName + "]<" + request.getRequestURI() + ">");
    }

    /**
     * 系统异常
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler({ Exception.class })
    @ResponseBody
    @ResponseStatus(value= HttpStatus.OK)
    public R<?> ExceptionHandler(HttpServletResponse response, Exception e) {
        String msg = "[" + applicationName + "]<" + e.getMessage() + "> " + e.getClass();
        LOGGER.error(msg, e);
        return R.failed(R.SERVER_ERROR, msg);
    }
    
    /**
     * 系统异常
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler({ RestException.class })
    @ResponseBody
    @ResponseStatus(value= HttpStatus.OK)
    public R<?> RestExceptionHandler(HttpServletResponse response, RestException e) {
        String msg = "[" + applicationName + "]<" + e.getMessage() + ">";
        // LOGGER.warn(msg, e);
        return R.failed(e.getCode(), msg);
    }

    /**
     * 系统异常
     * @param response
     * @param e
     * @return
     */
    @ExceptionHandler({ IllegalStateException.class })
    @ResponseBody
    @ResponseStatus(value= HttpStatus.OK)
    public R<?> IllegalStateExceptionHandler(HttpServletResponse response, IllegalStateException e) {
        String msg = "[" + applicationName + "]<" + e.getMessage() + "> " + e.getClass();
        LOGGER.warn(msg, e);
        return R.failed(R.FAILED, msg);
    }
    
	/**
	 * HttpMediaTypeNotSupportedException
	 * @param response
	 * @param e
	 * @return
	 */
	@ExceptionHandler({ HttpMediaTypeNotSupportedException.class })
	@ResponseBody
	@ResponseStatus(value= HttpStatus.OK)
	public R<?> HttpMediaTypeNotSupportedException(HttpServletResponse response, HttpMediaTypeNotSupportedException e) {
		return R.failed(R.ILLEGAL, "[" + applicationName + "]<" + e.getMessage() + ">");
	}

	/**
	 * HttpMediaTypeNotSupportedException
	 * @param response
	 * @param e
	 * @return
	 */
	@ExceptionHandler({ IllegalArgumentException.class })
	@ResponseBody
	@ResponseStatus(value= HttpStatus.OK)
	public R<?> IllegalArgumentException(HttpServletResponse response, IllegalArgumentException e) {
		return R.failed(R.FAILED, "[" + applicationName + "]<" + e.getMessage() + ">");
	}
	
	@ExceptionHandler({ MissingServletRequestParameterException.class })
	@ResponseBody
	@ResponseStatus(value= HttpStatus.OK)
	public R<?> MissingServletRequestParameterException(HttpServletResponse response, MissingServletRequestParameterException e) {
		return R.failed(R.ILLEGAL, "[" + applicationName + "]<" + e.getMessage() + ">");
	}
	
	@ExceptionHandler({ MethodArgumentTypeMismatchException.class })
	@ResponseBody
	@ResponseStatus(value= HttpStatus.OK)
	public R<?> MethodArgumentTypeMismatchException(HttpServletResponse response, MethodArgumentTypeMismatchException e) {
		return R.failed(R.ILLEGAL, "[" + applicationName + "]<" + e.getMessage() + ">");
	}
	
	@ExceptionHandler({ NumberFormatException.class })
	@ResponseBody
	@ResponseStatus(value= HttpStatus.OK)
	public R<?> NumberFormatException(HttpServletResponse response, NumberFormatException e) {
		return R.failed(R.ILLEGAL, "[" + applicationName + "]<" + e.getMessage() + ">");
	}
	
}
