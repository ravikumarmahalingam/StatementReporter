package com.cts.StatementProcessorDemo.ExceptionHandling;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

@ControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(CsvDataTypeMismatchException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "DataType mismatch in csv")
    public @ResponseBody ExceptionResponse handleResourceNotFound(final CsvDataTypeMismatchException exception,
            final HttpServletRequest request) {

        ExceptionResponse error = new ExceptionResponse();
        error.setErrorMessage(exception.getMessage());
        exception.getStackTrace();
        error.setRequestedURI(request.getRequestURI());
        return error;
    }

    @ExceptionHandler(CsvRequiredFieldEmptyException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Required Field empty")
    public @ResponseBody ExceptionResponse handleResourceNotFound(final CsvRequiredFieldEmptyException exception,
            final HttpServletRequest request) {

        ExceptionResponse error = new ExceptionResponse();
        error.setErrorMessage(exception.getMessage());
        error.setRequestedURI(request.getRequestURI());
        return error;
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "File IO Exception")
    public @ResponseBody ExceptionResponse handleResourceNotFound(final IOException exception,
            final HttpServletRequest request) {

        ExceptionResponse error = new ExceptionResponse();
        error.setErrorMessage(exception.getMessage());
        error.setRequestedURI(request.getRequestURI());
        return error;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Internal server issue")
    public @ResponseBody ExceptionResponse handleResourceNotFound(final Exception exception,
            final HttpServletRequest request) {

        ExceptionResponse error = new ExceptionResponse();
        error.setErrorMessage(exception.getMessage());
        error.setStackTraceList(exception.getStackTrace());
        return error;
    }
}
