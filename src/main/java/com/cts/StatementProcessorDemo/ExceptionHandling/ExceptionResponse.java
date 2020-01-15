package com.cts.StatementProcessorDemo.ExceptionHandling;

public class ExceptionResponse {

    private String errorMessage;
    private String requestedURI;
    private StackTraceElement[] stackTraceList;

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

    public StackTraceElement[] getStackTraceList() {
        return stackTraceList;
    }

    public void setStackTraceList(StackTraceElement[] stackTraceList) {
        this.stackTraceList = stackTraceList;
    }
}
