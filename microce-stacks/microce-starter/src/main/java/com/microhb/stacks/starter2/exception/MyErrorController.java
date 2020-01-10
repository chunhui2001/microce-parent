package com.microce.stacks.starter.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.microce.plugin.response.R;
import com.microce.plugin.response.RestException;

@Controller
public class MyErrorController implements ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    public void handleError(HttpServletRequest request, HttpServletResponse response)
            throws Throwable {

        // Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Object oe = request.getAttribute("javax.servlet.error.exception");

        if (oe == null)
            return;

        Throwable tr = null;

        if (oe instanceof RestException) {
            RestException restException = (RestException) oe;
            throw new RestException(restException.getCode(), restException.getMessage());
        }

        if (oe instanceof NullPointerException) {
            throw ((Exception) oe).getCause();
        }

        if (oe instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException) oe;
            throw new RestException(R.FAILED, runtimeException.getMessage());
        } 

        tr = ((Exception) oe).getCause();
        
        if (tr == null) {
            throw new RuntimeException(oe.getClass().toString());
        }

        if (tr.getCause() != null) {
            tr = tr.getCause();
            if (tr.getCause() != null) {
                tr = tr.getCause();
                if (tr.getCause() != null) {
                    tr = tr.getCause();
                    if (tr.getCause() != null) {
                        tr = tr.getCause();
                    }
                }
            }
        }

        throw tr;

    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
