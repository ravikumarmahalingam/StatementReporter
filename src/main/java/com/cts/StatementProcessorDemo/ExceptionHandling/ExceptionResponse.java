package com.cts.StatementProcessorDemo.ExceptionHandling;

import org.springframework.http.HttpStatus;

public class ExceptionResponse{

    private String errorMessage;
    private String requestedURI;
    private HttpStatus httpStatus;

    public ExceptionResponse(HttpStatus httpStatus, String errorMessage, String requestedURI) {
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
        this.requestedURI = requestedURI;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRequestedURI() {
        return requestedURI;
    }

    public void setRequestedURI(String requestedURI) {
        this.requestedURI = requestedURI;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
