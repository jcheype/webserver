package com.jcheype.webServer;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.io.ByteStreams;
import com.sampullara.mustache.Mustache;
import com.sampullara.mustache.MustacheBuilder;
import com.sampullara.mustache.MustacheException;
import org.filirom1.concoct.haml.HamlCompiler;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 17/3/12
 */
public class TemplatePojoRoute extends Route {
    private static final Logger logger = LoggerFactory.getLogger(TemplatePojoRoute.class);
    private final HamlCompiler hamlCompiler = new HamlCompiler();
    private final Cache<String, Mustache> templateCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors() + 1)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(CacheLoader.from(new Function<String, Mustache>() {
                @Override
                public Mustache apply(@Nullable String templatePath) {
                    try {
                        logger.debug("loading template: " + templatePath);

                        if (templatePath.endsWith(".haml")) {
                            logger.debug("URI: {}", this.getClass().getClassLoader().getResource(templatePath));
                            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);
                            String haml = new String(ByteStreams.toByteArray(resourceAsStream), CharsetUtil.UTF_8);
                            logger.debug("HAML: {}", haml);

                            String html = hamlCompiler.compile(haml);
                            logger.debug("HAML->HTML: {}", html);
                            return new MustacheBuilder().parse(html, new File(templatePath).getParent());
                        }
                        return new MustacheBuilder().parseFile(templatePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }
            ));
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

    private void renderWithTemplate(String templatePath, Map resultMap, Response response) throws IOException, ExecutionException, MustacheException {
        ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();
        ChannelBufferOutputStream channelBufferOutputStream = new ChannelBufferOutputStream(channelBuffer);
        PrintWriter printWriter = new PrintWriter(channelBufferOutputStream);
        templateCache.get(templatePath).execute(printWriter, resultMap);
        channelBufferOutputStream.close();

        response.write(channelBuffer);
    }

    private void renderRest(Request request, Response response, Object res) throws IOException {
        if (res != null) {
            Produces produces = method.getAnnotation(Produces.class);

            if (produces != null) {
                for (String produce : produces.value()) {
                    if (request.request.getHeader(HttpHeaders.Names.ACCEPT).contains(produce)) {
                        if (produce.contains("json")) {
                            response.writeJSON(res);
                            return;
                        } else if (produce.contains("xml")) {
                            response.writeXML(res);
                            return;
                        }
                    }
                }
            }
            response.write(res.toString());
            return;
        }

    }

    @Override
    public void handle(Request request, Response response, Map<String, String> map) throws Exception {
        Object args[] = genArgs(request, response, map);
        Object res = method.invoke(pojo, args);

        if (res instanceof Map && ((Map) res).containsKey("layout")) {
            Map resultMap = (Map) res;
            renderWithTemplate((String) resultMap.get("layout"), resultMap, response);
        } else {
            renderRest(request, response, map);
        }
    }
}
