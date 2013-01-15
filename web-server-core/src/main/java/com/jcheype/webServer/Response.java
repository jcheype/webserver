package com.jcheype.webServer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

import java.nio.charset.Charset;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 10/24/11
 */
public class Response {
//    private final static ObjectMapper mapper = new ObjectMapper();
//    private final static XStream xstreamXML = new XStream();

    public final ChannelHandlerContext ctx;
    public final HttpRequest request;


    public Response(ChannelHandlerContext ctx, HttpRequest request) {
        this.request = request;
        this.ctx = ctx;
    }

    public void write(byte[] data) {
        final ChannelBuffer buffer = ChannelBuffers.copiedBuffer(data);
        write(buffer);
    }

    public void write(ChannelBuffer buffer) {
        if(!ctx.getChannel().isWritable())
            return;
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        response.setHeader(HttpHeaders.Names.SERVER, "nuvoos server");

        setContentLength(response, buffer.readableBytes());
        response.setContent(buffer);

        if (isKeepAlive(request)) {
            response.setHeader(HttpHeaders.Names.CONNECTION, "Keep-Alive");
        }

        final ChannelFuture future = ctx.getChannel().write(response);
        if (!isKeepAlive(request)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void write(String data, Charset charset) {
        write(data.getBytes(charset));
    }

    public void write(String data) {
        write(data, CharsetUtil.UTF_8);
    }

    public void write(HttpResponse response) {
        if (isKeepAlive(request)) {
            response.setHeader(HttpHeaders.Names.CONNECTION, "Keep-Alive");
        }
        setContentLength(response, response.getContent().readableBytes());

        ChannelFuture future = ctx.getChannel().write(response);
        if (!isKeepAlive(request)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

//    public void writeJSON(Object object) throws IOException {
//        ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();
//        ChannelBufferOutputStream outputStream = new ChannelBufferOutputStream(channelBuffer);
//
//        //manage jsonp
//        List<String> callback = new QueryStringDecoder(request.getUri()).getParameters().get("callback");
//        if(callback != null && !callback.isEmpty()){
//            outputStream.writeBytes(callback.get(0)+"(");
//            mapper.writeValue(outputStream, object);
//            outputStream.writeBytes(")");
//        }
//        else{
//            mapper.writeValue(outputStream, object);
//        }
//        outputStream.close();
//        write(channelBuffer);
//    }
//
//    public void writeXML(Object object) {
//        ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();
//        ChannelBufferOutputStream outputStream = new ChannelBufferOutputStream(channelBuffer);
//        xstreamXML.toXML(object, outputStream);
//        write(channelBuffer);
//    }
}
