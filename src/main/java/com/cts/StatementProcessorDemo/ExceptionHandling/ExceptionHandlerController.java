package com.cts.StatementProcessorDemo.ExceptionHandling;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(Exception.class)
    public @ResponseBody ResponseEntity<ExceptionResponse> handleResourceNotFound(final ExceptionResponse exception,
            final HttpServletRequest request) {

        ExceptionResponse exceptionResponse = new ExceptionResponse(exception.getHttpStatus(),
                exception.getErrorMessage(), request.getRequestURI());
        exceptionResponse.setRequestedURI(request.getRequestURI());
        return new ResponseEntity<ExceptionResponse>(exceptionResponse, exceptionResponse.getHttpStatus());
    }
}
