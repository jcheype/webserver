package com.jcheype.webServer.spring;

import com.jcheype.webServer.HttpApiServerHandler;
import com.jcheype.webServer.RestHandler;
import com.jcheype.webServer.WebServerPipelinefactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: Julien Cheype
 * Date: 2/6/12
 */
@Component
public class SpringWebServer {
    @Autowired
    private RestHandler simpleRestHandler;
    private ServerBootstrap bootstrap = null;
    private Channel channel = null;
    private int port = 8888;
    private boolean autoStart = false;

    @PostConstruct
    public void init() {
        bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        bootstrap.setOption("writeBufferHighWaterMark", 10 * 64 * 1024);
        bootstrap.setOption("sendBufferSize", 1084576);
        bootstrap.setOption("receiveBufferSize", 1048576);
        bootstrap.setOption("tcpNoDelay", true);

        bootstrap.setPipelineFactory(new WebServerPipelinefactory(new HttpApiServerHandler(simpleRestHandler)));

        if(autoStart)
            start();
    }

    public void start(){
        channel = bootstrap.bind(new InetSocketAddress(port));
    }

    public void stop() {
        if (channel == null || bootstrap == null)
            throw new IllegalStateException("Server is not start");
        channel.close();
        channel = null;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
}
