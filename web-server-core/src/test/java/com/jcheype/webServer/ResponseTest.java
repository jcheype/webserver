package com.jcheype.webServer;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 15/3/12
 */
public class ResponseTest {
    HttpRequest httpRequestMock = mock(HttpRequest.class);
    ChannelHandlerContext channelHandlerContextMock = mock(ChannelHandlerContext.class);
    Channel channelMock = mock(Channel.class);


    @Test
    public void write_text() throws Exception {

        when(channelHandlerContextMock.getChannel()).thenReturn(channelMock);
        when(channelMock.isWritable()).thenReturn(true);
        when(httpRequestMock.getHeaders()).thenReturn(new ArrayList<Map.Entry<String, String>>());
        when(httpRequestMock.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        when(httpRequestMock.getUri()).thenReturn("/test");

        Response response = new Response(channelHandlerContextMock, httpRequestMock);
        response.write("hello");
        ArgumentCaptor<HttpResponse> responseArg = ArgumentCaptor.forClass(HttpResponse.class);

        verify(channelMock).write(responseArg.capture());

        String result = responseArg.getValue().getContent().toString(CharsetUtil.US_ASCII);
        assertEquals("hello", result);
        assertEquals(200, responseArg.getValue().getStatus().getCode());
    }

    @Test
    public void write_json() throws Exception {

        when(channelHandlerContextMock.getChannel()).thenReturn(channelMock);
        when(channelMock.isWritable()).thenReturn(true);
        when(httpRequestMock.getHeaders()).thenReturn(new ArrayList<Map.Entry<String, String>>());
        when(httpRequestMock.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        when(httpRequestMock.getUri()).thenReturn("/test");

        Response response = new Response(channelHandlerContextMock, httpRequestMock);
        response.writeJSON("hello");
        ArgumentCaptor<HttpResponse> responseArg = ArgumentCaptor.forClass(HttpResponse.class);

        verify(channelMock).write(responseArg.capture());

        assertEquals("\"hello\"", responseArg.getValue().getContent().toString(CharsetUtil.US_ASCII));
        assertEquals(200, responseArg.getValue().getStatus().getCode());
    }

    @Test
    public void write_xml() throws Exception {

        when(channelHandlerContextMock.getChannel()).thenReturn(channelMock);
        when(channelMock.isWritable()).thenReturn(true);
        when(httpRequestMock.getHeaders()).thenReturn(new ArrayList<Map.Entry<String, String>>());
        when(httpRequestMock.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        when(httpRequestMock.getUri()).thenReturn("/test");

        Response response = new Response(channelHandlerContextMock, httpRequestMock);
        response.writeXML("hello");

        ArgumentCaptor<HttpResponse> responseArg = ArgumentCaptor.forClass(HttpResponse.class);
        verify(channelMock).write(responseArg.capture());

        assertEquals("<string>hello</string>", responseArg.getValue().getContent().toString(CharsetUtil.US_ASCII));
        assertEquals(200, responseArg.getValue().getStatus().getCode());
    }
}
