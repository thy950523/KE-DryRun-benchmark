package io.kyligence.benchmark;

import lombok.Getter;

public class HttpRequestException extends Exception {

    @Getter
    private String result;

    @Getter
    private String message;

    @Getter
    private String errorCode;

    public HttpRequestException(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    public HttpRequestException(String _result) {
        result = _result;
    }



}