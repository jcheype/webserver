package com.jcheype.webServer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;


/**
 * Created with IntelliJ IDEA.
 * User: jcheype
 * Date: 04/01/13
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */
public class ResponseBuilder {
    HttpResponseStatus status = HttpResponseStatus.OK;
    HttpVersion version = HttpVersion.HTTP_1_1;

    Map<String, Iterable<String>> headers = new HashMap<String, Iterable<String>>();

    ChannelBuffer buffer;

    public ResponseBuilder setStatus(HttpResponseStatus status) {
        this.status = status;
        return this;
    }

    public ResponseBuilder setVersion(HttpVersion version) {
        this.version = version;
        return this;
    }

    public ResponseBuilder setContent(HttpVersion version) {
        this.version = version;
        return this;
    }

    public ResponseBuilder setHeader(String key, String value) {
        ArrayList<String> valueList = new ArrayList<String>(1);
        valueList.add(value);
        setHeader(key, valueList);
        return this;
    }

    public ResponseBuilder setHeader(String key, Iterable<String> valueIterable) {
        if (headers.containsKey(key))
            throw new IllegalStateException("header key is already set");
        headers.put(key, valueIterable);
        return this;
    }

    public ResponseBuilder setContent(String data, Charset charset) {
        setContent(data.getBytes(charset));
        return this;
    }

    public ResponseBuilder setContent(byte[] bytes) {
        buffer = ChannelBuffers.copiedBuffer(bytes);
        return this;
    }

    public ResponseBuilder setContent(ChannelBuffer buffer) {
        if (buffer != null)
            throw new IllegalStateException("content is already set");
        this.buffer = buffer;
        return this;
    }

    public HttpResponse build() {
        DefaultHttpResponse response = new DefaultHttpResponse(version, status);
        for (Map.Entry entry : headers.entrySet()) {
            response.setHeader((String) entry.getKey(), (Iterable<String>) entry.getValue());
        }

        if (buffer != null) {
            setContentLength(response, buffer.readableBytes());
            response.setContent(buffer);
        }
        return response;
    }
}
