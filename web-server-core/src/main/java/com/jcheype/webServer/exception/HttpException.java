package com.jcheype.webServer.exception;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 10/28/11
 */
public class HttpException extends Exception {
    private final HttpResponseStatus status;

    public HttpException(HttpResponseStatus status) {
        this(status, null);
    }

    public HttpException(HttpResponseStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}
