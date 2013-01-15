package com.jcheype.webServer.spring.component;

import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.jcheype.webServer.Request;
import com.jcheype.webServer.Response;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 2/9/12
 */
@Component
public class LongPolling {
//    private final Cache<String, List> msgCache = CacheBuilder.newBuilder()
//            .maximumSize(100000)
//            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
//            .expireAfterWrite(30, TimeUnit.SECONDS)
//            .build(CacheLoader.from(new Supplier<List>() {
//                public List get() {
//                    return new ArrayList();
//                }
//            }));
//
//    private final Cache<String, Response> ctxCache = CacheBuilder.newBuilder()
//            .maximumSize(100000)
//            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
//            .expireAfterWrite(30, TimeUnit.SECONDS)
//            .build(CacheLoader.from(new Supplier<Response>() {
//                public Response get() {
//                    return null;
//                }
//            }));
//
//    @GET
//    @Path("/push/poll/:uuid")
//    public void poll(Request request, Response response, String uuid) throws IOException {
//        List list = msgCache.getUnchecked(uuid);
//        //noinspection SynchronizationOnLocalVariableOrMethodParameter,SynchronizationOnLocalVariableOrMethodParameter
//        synchronized (list) {
//            if (list.isEmpty()) {
//                ctxCache.asMap().put(uuid, response);
//            } else{
//                response.writeJSON(list);
//                msgCache.invalidate(uuid);
//            }
//        }
//    }
//
//    @GET
//    @Path("/push/send/:uuid")
//    public void sendMessage(Request request, Response response, String uuid) throws IOException {
//        sendMessage(uuid, request.parameters);
//        response.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
//    }
//
//    public void sendMessage(String uuid, Object message) throws IOException {
//        List list = msgCache.getUnchecked(uuid);
//        //noinspection SynchronizationOnLocalVariableOrMethodParameter
//        synchronized (list) {
//            //noinspection unchecked
//            list.add(message);
//
//            ConcurrentMap<String, Response> ctxMap = ctxCache.asMap();
//            Response response = ctxMap.remove(uuid);
//            if (response != null) {
//                response.writeJSON(list);
//                msgCache.invalidate(uuid);
//            }
//        }
//    }
}
