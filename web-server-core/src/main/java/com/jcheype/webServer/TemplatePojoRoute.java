package com.jcheype.webServer;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheBuilder;
import com.sampullara.mustache.MustacheException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.Produces;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 17/3/12
 */
public class TemplatePojoRoute extends Route {
    private static final Logger logger = LoggerFactory.getLogger(TemplatePojoRoute.class);

    private final Object pojo;
    private final Method method;
    private final Class<?>[] parameterTypes;

    public TemplatePojoRoute(String path, Object pojo, Method method) {
        this(path, null, pojo, method);
    }

    public TemplatePojoRoute(String path, String contentType, Object pojo, Method method) {
        super(path, contentType);
        this.pojo = pojo;
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
    }

    private Object[] genArgs(Request request, Response response, Map<String, String> map) {
        Object args[] = new Object[parameterTypes.length];
        int i = 0;
        int pos = 0;
        for (Class clazz : parameterTypes) {
            if (clazz == Response.class) {
                args[i] = response;
            } else if (clazz == Request.class) {
                args[i] = request;
            } else {
                args[i] = map.get(getNames().get(pos++));
            }
            i++;
        }
        return args;
    }

    private void renderRest(Request request, Response response, Object res) throws IOException {
        if (res != null) {
            Produces produces = method.getAnnotation(Produces.class);
            response.write(res.toString());
            return;
        }

    }

    @Override
    public void handle(Request request, Response response, Map<String, String> map) throws Exception {
        Object args[] = genArgs(request, response, map);
        Object res = method.invoke(pojo, args);

        renderRest(request, response, res);

    }
}
