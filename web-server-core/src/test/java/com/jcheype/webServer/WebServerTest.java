package com.jcheype.webServer;

import com.jcheype.webServer.exception.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 15/3/12
 */
public class WebServerTest {
    private DefaultHttpClient httpClient = new DefaultHttpClient();
    private ServerBootstrap bootstrap;
    private SimpleRestHandler restHandler;
    private HttpApiServerHandler delfaultChannelHandler;
    private Channel channel;

    @Before
    public void setUp() throws Exception {
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        restHandler = new SimpleRestHandler();
        delfaultChannelHandler = new HttpApiServerHandler(restHandler);
        bootstrap.setPipelineFactory(new WebServerPipelinefactory(delfaultChannelHandler));

        channel = bootstrap.bind(new InetSocketAddress(9999));
    }

    @After
    public void tearDown() throws Exception {
        channel.close();
    }

    @Test
    public void webserver_404() throws Exception {
        HttpGet httpGet = new HttpGet("http://localhost:9999/404");
        HttpResponse response = httpClient.execute(httpGet);
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void webserver_rest_get() throws Exception {
        restHandler.get(new Route("/test") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                response.write("ok");
            }
        });

        HttpGet httpGet = new HttpGet("http://localhost:9999/test");
        HttpResponse response = httpClient.execute(httpGet);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void webserver_rest_post() throws Exception {
        restHandler.post(new Route("/testpost") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                response.write("ok");
            }
        });

        HttpPost httpPost = new HttpPost("http://localhost:9999/testpost");
        HttpResponse response = httpClient.execute(httpPost);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }


    @Test
    public void webserver_rest_put() throws Exception {
        restHandler.put(new Route("/testput") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                response.write("ok");
            }
        });

        HttpPut httpPut = new HttpPut("http://localhost:9999/testput");
        HttpResponse response = httpClient.execute(httpPut);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void webserver_rest_delete() throws Exception {
        restHandler.delete(new Route("/testdelete") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                response.write("ok");
            }
        });

        HttpDelete httpDelete = new HttpDelete("http://localhost:9999/testdelete");
        HttpResponse response = httpClient.execute(httpDelete);
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void webserver_rest_before_filter() throws Exception {
        restHandler.get(new Route("/test") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                response.write("ok");
            }
        });

        restHandler.before(new Route("/test") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                throw new HttpException(HttpResponseStatus.CONFLICT);
            }
        });

        HttpGet httpGet = new HttpGet("http://localhost:9999/test");
        HttpResponse response = httpClient.execute(httpGet);
        assertEquals(HttpResponseStatus.CONFLICT.getCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void webserver_rest_after_filter() throws Exception {
        final AtomicBoolean afterCall = new AtomicBoolean(false);
        restHandler.get(new Route("/test") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                response.write("ok");
            }
        });

        restHandler.after(new Route("/test") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                afterCall.set(true);
            }
        });

        HttpGet httpGet = new HttpGet("http://localhost:9999/test");
        HttpResponse response = httpClient.execute(httpGet);
        assertTrue(afterCall.get());
    }


    @Test
    public void webserver_exception() throws Exception {
        restHandler.get(new Route("/test") {
            @Override
            public void handle(Request request, Response response, Map<String, String> map) throws Exception {
                throw new Exception("Test exception");
            }
        });

        HttpGet httpGet = new HttpGet("http://localhost:9999/test");
        HttpResponse response = httpClient.execute(httpGet);
        assertEquals(500, response.getStatusLine().getStatusCode());
    }
}
